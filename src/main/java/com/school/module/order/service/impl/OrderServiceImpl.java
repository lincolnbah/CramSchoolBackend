package com.school.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Override
    public Order findByOrderNo(String orderNo) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getOrderNo, orderNo);
        return super.getOne(queryWrapper);
    }

    @Override
    public boolean updateOrderStatusOfAuto(Order order, String message, LocalDateTime paymentTime, String paymentType,
                                           BigDecimal fee, String thirdOrderNo, OrderStatusEnum orderStatusEnum) {
        OrderStatusEnum originalStatus = order.getStatus();
        if (originalStatus != OrderStatusEnum.PENDING && originalStatus != OrderStatusEnum.PROGRESSING) {
            return false;
        }
        if (StringUtils.isNotBlank(message)) {
            order.setMessage(message);
        }

        if (paymentTime != null) {
            order.setPaymentTime(paymentTime);
        }

        if (StringUtils.isNotBlank(paymentType)) {
            order.setPaymentType(paymentType);
        }

        if (fee != null) {
            order.setFee(fee);
        }

        if (StringUtils.isNotBlank(thirdOrderNo)) {
            order.setThirdOrderNo(thirdOrderNo);
        }

        if (orderStatusEnum != null) {
            order.setStatus(orderStatusEnum);
        }

        // Redis 防重複到期後仍可能並行，以資料庫原狀態作為更新條件。
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, order.getId()).eq(Order::getStatus, originalStatus);
        return super.update(order, wrapper);
    }

}
