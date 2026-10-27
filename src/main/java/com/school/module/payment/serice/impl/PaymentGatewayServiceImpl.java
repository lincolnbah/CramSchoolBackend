package com.school.module.payment.serice.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.module.payment.entity.PaymentGateway;
import com.school.module.payment.mapper.PaymentGatewayMapper;
import com.school.module.payment.serice.PaymentGatewayService;
import org.springframework.stereotype.Service;


@Service
public class PaymentGatewayServiceImpl extends ServiceImpl<PaymentGatewayMapper, PaymentGateway> implements PaymentGatewayService {

    @Override
    public PaymentGateway findById(Integer id) {
        return super.getById(id);
    }
}
