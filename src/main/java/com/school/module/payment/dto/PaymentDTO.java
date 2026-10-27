package com.school.module.payment.dto;

import com.school.common.enums.PaymentGatewayEnum;
import com.school.module.payment.entity.PaymentGateway;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
public class PaymentDTO {

    // 金額
    @Positive(message = "The amount must be greater than 0")
    private int amount;

    // 商品名稱
    @NotEmpty(message = "product name value is null")
    private List<@NotBlank(message = "product name is blank") String> productName;

    // 描述
    @NotBlank(message = "description is empty")
    private String description;

    // 訂單號
    private String orderNo;

    // 三方金流
    private PaymentGatewayEnum paymentGatewayEnum;

    // 金流配置
    private PaymentGateway PaymentGateway;

}
