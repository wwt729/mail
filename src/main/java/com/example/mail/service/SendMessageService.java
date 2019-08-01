package com.example.mail.service;

import com.example.mail.dto.MailDTO;
import com.example.mail.entity.Mail;
import com.example.mail.entity.MailInfo;
import com.example.mail.enums.SendStatusEnum;
import com.example.mail.mapper.MailInfoMapper;
import com.example.mail.mapper.MailMapper;
import com.example.mail.utils.BeanUtil;
import com.example.mail.utils.MailHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service("sendMailService")
@Slf4j
@EnableAsync  //开始异步支持
public class SendMessageService {

    @Autowired
    private MailHelper mailHelper;
    @Autowired
    private YbKafkaProducer ybKafkaProducer;
    @Autowired
    private MailMapper mailMapper;
    @Autowired
    private MailInfoMapper mailInfoMapper;

    @Value("${com.example.mail.sendNumber}")
    private Integer sendNumber;

    @Value("${com.example.mail.threadKillTime}")
    private Integer threadKillTime;


    @Async("asyncServiceExecutor")
    public void sendMessages(MailDTO mailDTO) {
        // 检查消息必要参数
        if (checkMail(mailDTO)) {
            return;
        }

        Mail mail = BeanUtil.copyProperties(mailDTO, Mail.class);
        MailInfo mailInfo = BeanUtil.copyProperties(mailDTO, MailInfo.class);

        Example example = new Example(Mail.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("mailUid", mailDTO.getMailUid());
        // 数据库同 mail_uid 的邮件数量
        int count = mailMapper.selectCountByExample(example);

        Date now = new Date();
        Date afterDate = new Date(now.getTime() + 60000);

        // 计划时间在当前时间 1 分钟之后的，首次只入库，不发邮件（等定时计划任务发送）
        if (mail.getPlanSendTime() != null && mail.getPlanSendTime().after(afterDate)) {
            if (count == 0) {
                mail.setStatus(SendStatusEnum.SEND_STATUS_PLAN.getStatus());// 默认未发送
                mailMapper.insert(mail);
                mailInfoMapper.insert(mailInfo);
            }
        }
        // 即时邮件发送（即时邮件 或 计划发送时间 比 当前时间+1分钟 小的）
        else {
            ListenableFuture<ListenableFuture> listenableFuture = new AsyncResult<>(new AsyncResult<>(true));
            try {
                // 首次发送，存入数据库
                if (count == 0) {
                    mailMapper.insert(mail);
                    mailInfoMapper.insert(mailInfo);
                }

                if ("365".equals(mailDTO.getServerFlag())) {
                    // 发送邮件（阻塞）
                    listenableFuture = mailHelper.sendMail365(mailDTO);
                    mailAddCallback(mailDTO, mail, listenableFuture);

                } else {
                    // 发送邮件（阻塞）
                    listenableFuture = mailHelper.sendMail114(mailDTO);
                    mailAddCallback(mailDTO, mail, listenableFuture);
                }

            } catch (Exception e) {
                if (listenableFuture != null) {
                    listenableFuture.cancel(true);
                    // 将当前消息重新生产到队列
                    log.info("发送出错,异常信息{}{}", e.getStackTrace(), e.getSuppressed());
                    tryAgain(mailDTO);
                } else {
                    log.info("listenableFuture未赋值，空指针异常");
                }
            }
        }
    }

    private void mailAddCallback(MailDTO mailDTO, Mail mail, ListenableFuture<ListenableFuture> listenableFuture) throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {

        listenableFuture.addCallback(listenableFuture1 -> {
            mail.setStatus(SendStatusEnum.SEND_STATUS_SUCCESS.getStatus());// 发送成功
            mail.setSendTime(new Date());
            // 更新数据库消息发送状态
            Example successExample = new Example(Mail.class);
            Example.Criteria successCriteria = successExample.createCriteria();
            successCriteria.andEqualTo("mailUid", mailDTO.getMailUid());
            mail.setSendNum(mailDTO.getSendNum() + 1);
            mailMapper.updateByExampleSelective(mail, successExample);
            log.info("发送邮件成功{}：{}->{}", mailDTO.getMailUid(), mailDTO.getFromMail(), mailDTO.getToMail());
        }, throwable -> {
            // 发送失败:状态置为2，设置mail的重试次数，mailDTO的sendnum是0-3，所以0/1的情况，mail的发送次数都为1；其他情况为dto的sendnum值
            Example failExample = new Example(Mail.class);
            Example.Criteria failCriteria = failExample.createCriteria();
            failCriteria.andEqualTo("mailUid", mailDTO.getMailUid());
            if (mailDTO.getSendNum() == 0) {
                mail.setSendNum(1);
            } else {
                mail.setSendNum(mailDTO.getSendNum());
            }
            mail.setStatus(SendStatusEnum.SEND_STATUS_FAIL.getStatus());
            // 更新数据库消息发送状态
            mailMapper.updateByExampleSelective(mail, failExample);
        });

        listenableFuture.get(threadKillTime, TimeUnit.SECONDS);
    }

    /**
     * 发送异常，将重新写入队列
     *
     * @param mailDTO
     */
    private void tryAgain(MailDTO mailDTO) {
        int num = mailDTO.getSendNum();
        // 如果，当前发送次数 < 邮件失败重试次数
        if (num < sendNumber) {
            mailDTO.setSendNum(num + 1);
            // 将当前消息重新生产到队列
            ybKafkaProducer.sendToKafkaStandardMessageAsync(mailDTO);
            log.info("{}出现异常，第 {} 次重新发送", mailDTO.getMailUid(), num);
        }
    }

    /**
     * 检查邮件
     *
     * @param mailDTO
     */
    private boolean checkMail(MailDTO mailDTO) {
        boolean check = false;

        //收件人不能为空
        if (StringUtils.isEmpty(mailDTO.getToMail())) {
            log.info("出现异常，收件人不能为空：{}", mailDTO);
            check = true;
        }

        //邮件主题不能为空
        if (StringUtils.isEmpty(mailDTO.getMailSubject())) {
            log.info("出现异常，邮件主题不能为空：{}", mailDTO);
            check = true;
        }

        // 邮件服务器标识：114、365
        String serverFlag = mailDTO.getServerFlag();
        // 邮件服务器标识不能为空，为空时异常
        if (StringUtils.isBlank(serverFlag)) {
            log.info("出现异常，邮件服务器标识不能为空：{}", mailDTO);
            check = true;
        }

        return check;
    }
}
