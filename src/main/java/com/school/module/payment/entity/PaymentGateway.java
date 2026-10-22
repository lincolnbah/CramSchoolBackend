package com.school.module.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "金流配置")
@TableName("payment_gateway")
public class PaymentGateway {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主鍵")
    private Integer id;

    @Schema(description = "商戶id")
    private String merchantId;

    @Schema(description = "密鑰")
    private String hashKey;

    @Schema(description = "iv")
    private String hashIv;

    @Schema(description = "支付完成用戶跳轉頁面")
    private String returnUrl;

    @Schema(description = "金流三方api")
    private String apiUrl;

}
