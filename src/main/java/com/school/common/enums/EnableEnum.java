package com.school.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum EnableEnum {

    DISABLE(0),
    ENABLE(1),

    ;

    @EnumValue
    private final Integer code;

    public Integer getCode() {
        return this.code;
    }

    EnableEnum(Integer code) {
        this.code = code;
    }
}
