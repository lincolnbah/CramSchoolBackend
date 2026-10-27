package com.school.module.payment.serice.impl;

import com.school.common.common.api.CommonCode;
import com.school.common.common.constant.RedisKey;
import com.school.common.common.exception.ApiException;
import com.school.common.enums.PaymentGatewayEnum;
import com.school.common.service.RedisService;
import com.school.common.utils.UserUtil;
import com.school.module.order.entity.Order;
import com.school.module.order.enums.OrderStatusEnum;
import com.school.module.order.enums.StatusEnum;
import com.school.module.order.service.OrderService;
import com.school.module.payment.dto.PaymentDTO;
import com.school.module.payment.dto.PaymentNotifyDTO;
import com.school.module.payment.entity.PaymentGateway;
import com.school.module.payment.handler.PaymentHandler;
import com.school.module.payment.serice.PaymentGatewayService;
import com.school.module.payment.serice.PaymentService;
import com.school.module.payment.vo.PaymentNotifyVO;
import com.school.module.payment.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${status.channel:1}")
    private int channel;

    private final PaymentGatewayService paymentGatewayService;

    private final OrderService orderService;

    private final Map<String, PaymentHandler> paymentHandlerMap;

    private final RedisService redisService;

    // todo 這邊改成update order status到處理中, 前面再做一隻確認訂單api, 建立pending訂單
    @Override
    public PaymentVO generatePayment(PaymentDTO paymentDto) {
        PaymentGatewayEnum paymentGatewayEnum = PaymentGatewayEnum.convertPaymentGatewayEnum(channel);

        if (paymentGatewayEnum == null) {
            log.error("金流未實作, channel:{}", channel);
            throw new ApiException(CommonCode.PAYMENT_SERVER_ERROR);
        }

        PaymentGateway paymentGateway = paymentGatewayService.findById(channel);
        paymentDto.setPaymentGateway(paymentGateway);

        Order order = orderService.buildOrder(UserUtil.get().getAccountId(), BigDecimal.valueOf(paymentDto.getAmount()), paymentDto.getPaymentGatewayEnum());
        paymentDto.setOrderNo(order.getOrderNo());
        PaymentHandler paymentHandler = paymentHandlerMap.get(paymentGatewayEnum.name().toLowerCase(Locale.ROOT));
        return paymentHandler.createPayment(paymentDto);
    }

    @Override
    public String notify(PaymentNotifyDTO paymentNotifyDto) {
        PaymentGatewayEnum paymentGatewayEnum = paymentNotifyDto.getPaymentGatewayEnum();
        String paymentText = paymentGatewayEnum.getText();

        PaymentHandler paymentHandler = paymentHandlerMap.get(paymentGatewayEnum.name().toLowerCase(Locale.ROOT));

        String notifyPrint = paymentHandler.notifyPrint();
        String orderNo = paymentNotifyDto.getRequestMap().get(paymentHandler.orderKey());
        if (StringUtils.isBlank(orderNo)) {
            log.error("[{}] 訂單號異常, 參數key無訂單號值, key:{}", paymentText, paymentHandler.orderKey());
            return notifyPrint;
        }

        String lockKey = RedisKey.PAYMENT_NOTIFY_LOCK + orderNo;
        boolean isLock = redisService.setIfAbsent(lockKey, "", RedisKey.PAYMENT_NOTIFY_TIME, TimeUnit.SECONDS);
        if (!isLock) {
            log.info("[{}][{}], redis lock", paymentText, orderNo);
            return notifyPrint;
        }

        Order order = orderService.findByOrderNo(orderNo);
        if (order == null) {
            log.error("[{}] 無此訂單資料, 訂單號:{}", paymentText, orderNo);
            return notifyPrint;
        }

        if (!Objects.equals(order.getStatus(), OrderStatusEnum.PROGRESSING)) {
            log.error("[{}] 訂單原始狀態非付款中, 訂單號:{}", paymentText, orderNo);
            return notifyPrint;
        }

        PaymentGateway paymentGateway = paymentGatewayService.findById(paymentGatewayEnum.getCode());
        PaymentNotifyVO paymentNotifyVo = paymentHandler.verifyNotification(paymentNotifyDto.getRequestMap(), paymentNotifyDto.getHeaderMap(), paymentGateway);

        // 訂單失敗處理(系統錯誤, 三方錯誤, 驗簽錯誤)
        if (Objects.equals(paymentNotifyVo.getStatusEnum(), StatusEnum.SERVER_ERROR) ||
                paymentNotifyVo.isSignError() || paymentNotifyVo.isStatusError()) {
            String errorTitle = paymentNotifyVo.isSignError() ? "驗簽失敗" : paymentNotifyVo.isStatusError() ? "其他原因" : "金流發生錯誤";
            log.error("[{}] 訂單號:{} {}錯誤, msg:{}", paymentText, orderNo, errorTitle, paymentNotifyVo.getOrderMessage());

            orderService.updateOrderStatusOfAuto(order, paymentNotifyVo.getOrderMessage(), null, null, null, null, OrderStatusEnum.FAILED);
            return notifyPrint;
        }

        // 實際金額與訂單金額不匹配
        if (order.getAmount().compareTo(paymentNotifyVo.getAmount()) != 0) {
            log.error("[{}][{}] 訂單金額異常 不匹配, 訂單實際金額為:{}, 三方回調實際金額為:{}",
                    paymentText, orderNo, order.getAmount(), paymentNotifyVo.getAmount());
            orderService.updateOrderStatusOfAuto(order, "實際付款金額與訂單不匹配", null, null, null, null, OrderStatusEnum.FAILED);
            return notifyPrint;
        }

        LocalDateTime payTime = paymentNotifyVo.getPaymentTime() == null ? LocalDateTime.now() : paymentNotifyVo.getPaymentTime();
        OrderStatusEnum orderStatusEnum = paymentNotifyVo.isSimulatePaid() ? OrderStatusEnum.SUCCESS_SIMULATE_PAID : OrderStatusEnum.SUCCESS;
        orderService.updateOrderStatusOfAuto(order, paymentNotifyVo.getOrderMessage(), payTime, paymentNotifyVo.getPaymentType(), paymentNotifyVo.getFee(), paymentNotifyVo.getThirdOrderNo(), orderStatusEnum);

        return notifyPrint;
    }
}
