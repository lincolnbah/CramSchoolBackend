package com.school.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.school.common.enums.PaymentGatewayEnum;
import com.school.module.order.enums.OrderStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Schema(description = "金流配置")
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主鍵")
    private Integer id;

    @Schema(description = "用戶id")
    private Integer accountId;

    @Schema(description = "訂單號")
    private String orderNo;

    @Schema(description = "金流方訂單號")
    private String thirdOrderNo;

    @Schema(description = "金額")
    private BigDecimal amount;

    @Schema(description = "金流方手續費")
    private BigDecimal fee;

    @Schema(description = "用戶付款方式")
    private String paymentType;

    @Schema(description = "用戶付款時間")
    private LocalDateTime paymentTime;

    @Schema(description = "訂單狀態")
    private OrderStatusEnum status;

    @Schema(description = "三方金流")
    private PaymentGatewayEnum paymentGateway;

    @Schema(description = "三方交易訊息")
    private String message;

    @Schema(description = "更新時間")
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    @Schema(description = "帳號建立時間，由資料庫預設填入目前時間")
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

}
