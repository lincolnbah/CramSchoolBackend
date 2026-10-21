package com.school.module.payment.serice;

import com.school.module.payment.dto.PaymentDTO;
import com.school.module.payment.vo.PaymentNotificationVO;
import com.school.module.payment.vo.PaymentVO;

import java.util.Map;

/**
 * Hosted credit-card checkout. All amounts are integer TWD.
 */
public interface PaymentService {
    PaymentVO createPayment(PaymentDTO request);

    /**
     * Pass decoded form fields once, and expected values loaded from your own order store.
     */
    PaymentNotificationVO verifyNotification(Map<String, String> fields,
                                             String expectedOrderNo, int expectedAmount);
}
