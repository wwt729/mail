package com.example.mail.entity;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "mail_info")
@Data
public class MailInfo {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Integer id;

    //邮件发送方唯一标识（发送方生成）
    private String mailUid;
    //邮件主题
    private String mailSubject;
    //邮件内容
    private String mailContent;
}
