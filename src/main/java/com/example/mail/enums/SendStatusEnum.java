package com.example.mail.enums;

public enum SendStatusEnum {

    //计划0，发送成功1，发送失败2，发送中3
    SEND_STATUS_PLAN(0),
    SEND_STATUS_SUCCESS(1),
    SEND_STATUS_FAIL(2),
    SEND_STATUS_ING(3),
    ;

    private Integer status;
    SendStatusEnum(Integer status) {
        this.status=status;
    }
    public Integer getStatus(){
        return status;
    }
}
