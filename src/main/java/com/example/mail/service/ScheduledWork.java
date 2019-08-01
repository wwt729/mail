package com.example.mail.service;


import com.example.mail.dto.MailDTO;
import com.example.mail.entity.Mail;
import com.example.mail.enums.SendStatusEnum;
import com.example.mail.mapper.MailMapper;
import com.example.mail.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ScheduledWork {

    @Autowired
    private YbKafkaProducer ybKafkaProducer;
    @Autowired
    private MailMapper mailMapper;


    @Scheduled(cron = "0/10 * * * * *")
    public void scheduled() throws ParseException {

        int num = new Random().nextInt(2) + 1;
        String serverFlag = "114";
        if (num == 1) {
            serverFlag = "365";
        }

        MailDTO mailDTO = new MailDTO();
        mailDTO.setMailUid(UUID.randomUUID().toString());
        mailDTO.setFromName("收件人别名");
        mailDTO.setFromMail("发件人邮箱");
        mailDTO.setToMail("收件人邮箱");
//            mailDTO.setPlanSendTime(parseDateStr("2019-07-26 16:50:52", "yyyy-MM-dd HH:mm:ss"));
        mailDTO.setMailSubject("主题");
        mailDTO.setMailContent("测试正文");
        mailDTO.setServerFlag(serverFlag);
        ybKafkaProducer.sendToKafkaStandardMessageAsync(mailDTO);

    }

    public static Date parseDateStr(String dateStr, String simpleDataType) throws ParseException {
        DateFormat format = new SimpleDateFormat(simpleDataType);
        return format.parse(dateStr);
    }

    /**
     * 定时邮件处理
     * 5分钟扫描一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void planSendMail() {
        synchronized (this) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar nowTimeEnd = Calendar.getInstance();
            String endTime = sdf.format(nowTimeEnd.getTime());

            // 查找已到计划发送时间的邮件
            List<MailDTO> list = mailMapper.selectByTime(endTime);

            for (MailDTO dto : list) {
                // 生产消息
                ybKafkaProducer.sendToKafkaStandardMessageAsync(dto);

                // 邮件发送中
                Mail mail = BeanUtil.copyProperties(dto, Mail.class);
                mail.setStatus(SendStatusEnum.SEND_STATUS_ING.getStatus());
                mailMapper.updateByPrimaryKeySelective(mail);
            }

        }
    }

}
