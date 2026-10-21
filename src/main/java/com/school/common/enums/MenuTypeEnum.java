package com.school.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum MenuTypeEnum {

    MENU(1, "列表"),
    BUTTON(2, "按鈕"),

    ;

    @EnumValue
    private final Integer code;

    private final String text;

    public Integer getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    MenuTypeEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

}
