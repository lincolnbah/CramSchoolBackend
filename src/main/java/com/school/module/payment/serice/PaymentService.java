package com.school.module.payment.serice;

import com.school.module.payment.dto.PaymentDTO;
import com.school.module.payment.dto.PaymentNotifyDTO;
import com.school.module.payment.vo.PaymentVO;

public interface PaymentService {

    PaymentVO generatePayment(PaymentDTO paymentDto);

    String notify(PaymentNotifyDTO paymentNotifyDto);

}
