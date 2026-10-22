package com.school.module.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum OrderStatusEnum {

    PENDING(1, "Pending"),
    PROGRESSING(2, "Processing"),
    FAILED(3, "Failed"),
    SUCCESS(4, "Success"),
    CANCEL(5, "Cancel"),

    ;

    @EnumValue
    private final int code;

    private final String text;

    OrderStatusEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

}
