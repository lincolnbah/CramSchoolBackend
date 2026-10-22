package com.school.module.order.service;

import com.school.common.enums.PaymentGatewayEnum;
import com.school.module.order.entity.Order;

import java.math.BigDecimal;

public interface OrderService {

    Order buildOrder(Integer accountId, BigDecimal amount, PaymentGatewayEnum paymentGatewayEnum);

}
