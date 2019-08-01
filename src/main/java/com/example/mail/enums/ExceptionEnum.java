package com.example.mail.enums;

public enum  ExceptionEnum {

    DATA_TRANSFER_ERROR(500, "数据转换异常！"),
    MAIL_SEND_FAIL(500,"邮件发送失败"),
    TOMAIL_NOT_NULL(400,"收件人不能为空"),
    MAILSUBJECT_NOT_NULL(400,"邮件主题不能为空"),

    SERVERFLAG_NOT_NULL(400,"邮件服务器不能为空");


    private int status;
    private String message;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    ExceptionEnum(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
