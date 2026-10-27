package com.school.module.order.enums;

import lombok.Getter;

@Getter
public enum StatusEnum {


    SERVER_ERROR(1, "系統錯誤"),

    SIGN_CHECK_ERROR(2, "驗簽失敗"),

    NOTIFY_FAIL(3, "三方回調失敗"),

    ;

    private final int code;

    private final String text;

    StatusEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

}
