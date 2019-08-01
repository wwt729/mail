package com.example.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailDTO {
    // 更新时使用
    private Integer id;

    //邮件唯一标识（发送方生成）
    private String mailUid;
    //邮件发送方名称
    private String fromName;
    //发送邮件地址
    private String fromMail;
    //接收邮件地址（多个邮箱则用逗号","隔开）
    private String toMail;
    //抄送邮件地址（多个邮箱则用逗号","隔开）
    private String ccMail;
    //密送邮件地址（多个邮箱则用逗号","隔开）
    private String bccMail;
    //计划发送时间
    private Date planSendTime;
    //邮件发送时间
    private Date sendTime;
    //邮件主题
    private String mailSubject;
    //邮件内容
    private String mailContent;
    //发送次数
    private Integer sendNum = 0;
    //邮件服务器标识
    private String serverFlag;

}
