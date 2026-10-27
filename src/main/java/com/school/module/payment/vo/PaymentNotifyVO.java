package com.school.module.payment.vo;

import com.school.module.order.enums.OrderStatusEnum;
import com.school.module.order.enums.StatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class PaymentNotifyVO {

    private OrderStatusEnum orderStatusEnum;

    private String orderMessage;

    private LocalDateTime paymentTime;

    private String paymentType;

    private BigDecimal amount;

    private BigDecimal fee;

    private String thirdOrderNo;

    private boolean signError;

    private boolean statusError;

    // 模擬通知只確認接收，不更新真實訂單。
    private boolean simulatePaid = Boolean.FALSE;

    private StatusEnum statusEnum;

}
