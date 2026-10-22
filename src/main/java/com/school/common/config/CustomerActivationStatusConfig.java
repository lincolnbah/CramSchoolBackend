package com.school.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class CustomerActivationStatusConfig {

    @Value("${status.payment-gateway:0}")
    private int paymentGatewayStatus;
}
