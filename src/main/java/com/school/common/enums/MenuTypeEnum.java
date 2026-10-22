package com.school.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MenuTypeEnum {

    MENU(1, "列表"),
    BUTTON(2, "按鈕"),

    ;

    @EnumValue
    private final Integer code;

    private final String text;

    MenuTypeEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

}
