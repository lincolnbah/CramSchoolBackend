package com.school.module.payment.handler;

import com.school.module.payment.dto.PaymentDTO;
import com.school.module.payment.entity.PaymentGateway;
import com.school.module.payment.vo.PaymentNotifyVO;
import com.school.module.payment.vo.PaymentVO;

import java.util.Map;

public interface PaymentHandler {

    String orderKey();

    String notifyPrint();

    PaymentVO createPayment(PaymentDTO paymentDto);

    PaymentNotifyVO verifyNotification(Map<String, String> paramsMap, Map<String, String> headerMap, PaymentGateway paymentGateway);

}
