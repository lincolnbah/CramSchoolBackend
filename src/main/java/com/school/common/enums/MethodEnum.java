package com.school.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MethodEnum {

    GET(1),
    POST(2),
    PUT(3),
    PATCH(4),
    DELETE(5),

    ;

    @EnumValue
    private final Integer code;

    MethodEnum(Integer code) {
        this.code = code;
    }
}
