package com.school.module.order.service;

import com.school.common.enums.PaymentGatewayEnum;
import com.school.module.order.entity.Order;
import com.school.module.order.enums.OrderStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OrderService {

    Order buildOrder(Integer accountId, BigDecimal amount, PaymentGatewayEnum paymentGatewayEnum);

    Order findByOrderNo(String orderNo);

    boolean updateOrderStatusOfAuto(Order order, String message, LocalDateTime paymentTime, String paymentType, BigDecimal fee, String thirdOrderNo, OrderStatusEnum orderStatusEnum);

}
