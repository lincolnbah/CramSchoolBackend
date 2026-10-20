package com.school.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum MethodEnum {

    GET(1),
    POST(2),
    PUT(3),
    PATCH(4),
    DELETE(5),

    ;

    @EnumValue
    private final Integer code;

    public Integer getCode() {
        return this.code;
    }

    MethodEnum(Integer code) {
        this.code = code;
    }
}
