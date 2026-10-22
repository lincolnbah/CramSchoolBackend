package com.school.module.payment.serice.impl;

import com.school.common.common.api.CommonCode;
import com.school.common.common.exception.ApiException;
import com.school.common.utils.UserUtil;
import com.school.module.order.entity.Order;
import com.school.module.order.service.OrderService;
import com.school.module.payment.dto.PaymentDTO;
import com.school.common.enums.PaymentGatewayEnum;
import com.school.module.payment.handler.PaymentHandler;
import com.school.module.payment.serice.PaymentService;
import com.school.module.payment.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${status.channel:1}")
    private int channel;

    private final OrderService orderService;

    private final Map<String, PaymentHandler> paymentHandlerMap;


    @Override
    public PaymentVO generatePayment(PaymentDTO paymentDto) {
        PaymentGatewayEnum paymentGatewayEnum = PaymentGatewayEnum.convertPaymentGatewayEnum(channel);

        if (paymentGatewayEnum == null) {
            log.error("金流未實作, channel:{}", channel);
            throw new ApiException(CommonCode.PAYMENT_SERVER_ERROR);
        }

        Order order = orderService.buildOrder(UserUtil.get().getAccountId(), BigDecimal.valueOf(paymentDto.getAmount()), paymentDto.getPaymentGateway());
        paymentDto.setOrderNo(order.getOrderNo());
        PaymentHandler paymentHandler = paymentHandlerMap.get(paymentGatewayEnum.name().toLowerCase(Locale.ROOT));
        // todo 待處理
        return null;
    }
}
