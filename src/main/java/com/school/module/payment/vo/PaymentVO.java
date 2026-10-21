package com.school.module.payment.vo;

import lombok.Data;

import java.util.Map;

@Data
public final class PaymentVO {

    private String actionUrl;

    private Map<String, Object> fields;

}
