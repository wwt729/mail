package com.example.mail.service.impl;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class MyJavaMail114SenderImpl extends JavaMailSenderImpl {


    public MyJavaMail114SenderImpl() {
        this.setHost("服务器地址");
        this.setPort(25);//端口
        this.setUsername("账号");
        this.setPassword("密码");
        Properties properties114 = new Properties();
        properties114.setProperty("from", "账号");
        properties114.setProperty("mail.smtp.auth", "true");
        properties114.setProperty("mail.smtp.connectiontimeout", "50000");
        properties114.setProperty("mail.smtp.timeout","50000");
        properties114.setProperty("mail.smtp.writetimeout","50000");
        this.setJavaMailProperties(properties114);
    }
}
