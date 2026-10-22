package com.school.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum PaymentGatewayEnum {

    MANUAL(0, "手動非金流"),
    ECPAY(1, "綠界"),

    ;

    @EnumValue
    private final int code;

    private final String text;

    PaymentGatewayEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static PaymentGatewayEnum convertPaymentGatewayEnum(int code) {
        for (PaymentGatewayEnum paymentGatewayEnum : PaymentGatewayEnum.values()) {
            if (Objects.equals(paymentGatewayEnum.getCode(), code)) {
                return paymentGatewayEnum;
            }
        }
        return null;
    }

}
