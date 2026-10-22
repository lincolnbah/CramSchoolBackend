package com.school.module.payment.serice.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.module.payment.entity.PaymentGateway;
import com.school.module.payment.mapper.PaymentGatewayMapper;
import com.school.module.payment.serice.PaymentGatewayService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentGatewayServiceImpl extends ServiceImpl<PaymentGatewayMapper, PaymentGateway> implements PaymentGatewayService {

    @Override
    public PaymentGateway findOne() {
        List<PaymentGateway> paymentGatewayList = super.list();
        if (CollUtil.isNotEmpty(paymentGatewayList)) {
            return paymentGatewayList.get(0);
        }
        return new PaymentGateway();
    }
}
