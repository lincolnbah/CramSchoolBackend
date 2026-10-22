package com.school.module.payment.handler;

import com.school.module.payment.dto.PaymentDTO;
import com.school.module.payment.vo.PaymentVO;

import java.util.Map;

public interface PaymentHandler {

    PaymentVO createPayment(PaymentDTO paymentDto);

    String verifyNotification(Map<String, String> fields);

}
