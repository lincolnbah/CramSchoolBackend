package com.school.module.payment.dto;

import com.school.common.enums.PaymentGatewayEnum;
import com.school.module.payment.entity.PaymentGateway;
import lombok.Data;

import java.util.Map;

@Data
public class PaymentNotifyDTO {

    private PaymentGatewayEnum paymentGatewayEnum;

    private PaymentGateway paymentGateway;

    private Map<String, String> requestMap;

    private Map<String, String> headerMap;

}
