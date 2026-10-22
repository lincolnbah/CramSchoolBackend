package com.school.module.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.common.common.api.CommonCode;
import com.school.common.common.exception.ApiException;
import com.school.common.enums.PaymentGatewayEnum;
import com.school.module.order.entity.Order;
import com.school.module.order.enums.OrderStatusEnum;
import com.school.module.order.mapper.OrderMapper;
import com.school.module.order.service.OrderService;
import com.school.module.order.utils.OrderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {


    @Override
    public Order buildOrder(Integer accountId, BigDecimal amount, PaymentGatewayEnum paymentGatewayEnum) {
        Order order = new Order();
        order.setAccountId(accountId);
        order.setOrderNo(OrderUtil.buildOrderNo());
        order.setAmount(amount);
        order.setStatus(OrderStatusEnum.PENDING);
        order.setPaymentGateway(paymentGatewayEnum);
        boolean isSave = super.save(order);
        if (!isSave) {
            throw new ApiException(CommonCode.ORDER_CREATION_FAILED);
        }
        return order;
    }
}
