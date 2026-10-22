package com.school.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum EnableEnum {

    DISABLE(0),
    ENABLE(1),

    ;

    @EnumValue
    private final Integer code;

    EnableEnum(Integer code) {
        this.code = code;
    }
}
