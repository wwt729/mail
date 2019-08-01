package com.example.mail.utils;

import com.example.mail.dto.MailDTO;
import com.example.mail.service.impl.MyJavaMail114SenderImpl;
import com.example.mail.service.impl.MyJavaMail365SenderImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;

@Slf4j
@Component
@EnableAsync  //开始异步支持
public class MailHelper {

    //注入邮件工具类
    @Autowired
    private MyJavaMail365SenderImpl mail365Sender;

    @Autowired
    private MyJavaMail114SenderImpl mail114Sender;


    @Async("asyncServiceSonExecutor")
    public ListenableFuture<ListenableFuture> sendMail365(MailDTO mailDTO) throws Exception {

        // 邮件服务器标识：365
        String serverFrom = (String) mail365Sender.getJavaMailProperties().get("from");

        MimeMessageHelper messageHelper = new MimeMessageHelper(mail365Sender.createMimeMessage(), true);//true表示支持复杂类型
        setMessagePro(mailDTO, serverFrom, messageHelper);
        //正式发送邮件
        mail365Sender.send(messageHelper.getMimeMessage());

        return new AsyncResult<>(new AsyncResult<>(true));
    }

    @Async("asyncServiceSonExecutor")
    public ListenableFuture<ListenableFuture> sendMail114(MailDTO mailDTO) throws Exception {

        // 邮件服务器标识：114
        String serverFrom = (String) mail114Sender.getJavaMailProperties().get("from");

        MimeMessageHelper messageHelper = new MimeMessageHelper(mail114Sender.createMimeMessage(), true);//true表示支持复杂类型
        setMessagePro(mailDTO, serverFrom, messageHelper);

        //正式发送邮件
        mail114Sender.send(messageHelper.getMimeMessage());

        return new AsyncResult<>(new AsyncResult<>(true));
    }

    private void setMessagePro(MailDTO mailDTO, String serverFrom, MimeMessageHelper messageHelper) throws MessagingException, UnsupportedEncodingException {
        String fromMail = mailDTO.getFromMail();
        if (StringUtils.isBlank(fromMail)) {
            fromMail = serverFrom;
        }
        messageHelper.setFrom(new InternetAddress(serverFrom, mailDTO.getFromName(), "UTF-8"));
        messageHelper.setReplyTo(fromMail, mailDTO.getFromName());// 回信地址
        messageHelper.setTo(mailDTO.getToMail().split(","));//邮件收信人
        messageHelper.setSubject(mailDTO.getMailSubject());//邮件主题
        messageHelper.setText(mailDTO.getMailContent());//邮件内容
        if (!StringUtils.isEmpty(mailDTO.getCcMail())) {//抄送
            messageHelper.setCc(mailDTO.getCcMail().split(","));
        }
        if (!StringUtils.isEmpty(mailDTO.getBccMail())) {//密送
            messageHelper.setCc(mailDTO.getBccMail().split(","));
        }
    }

}
