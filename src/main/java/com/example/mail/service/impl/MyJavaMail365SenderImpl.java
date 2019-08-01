package com.example.mail.service.impl;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class MyJavaMail365SenderImpl extends JavaMailSenderImpl  {


    public MyJavaMail365SenderImpl() {
        this.setHost("服务器地址");
        this.setPort(25);//端口
        this.setUsername("账号");
        this.setPassword("密码");
        Properties properties365 = new Properties();
        properties365.setProperty("from", "账号");
        properties365.setProperty("mail.smtp.starttls.enable", "true");
        properties365.setProperty("mail.smtp.socketFactory.fallback", "true");
        properties365.setProperty("mail.smtp.connectiontimeout", "50000");
        properties365.setProperty("mail.smtp.timeout","50000");
        properties365.setProperty("mail.smtp.writetimeout","50000");
        this.setJavaMailProperties(properties365);
    }

}
