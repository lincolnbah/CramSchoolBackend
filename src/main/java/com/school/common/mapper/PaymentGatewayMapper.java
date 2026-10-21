package com.school.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.module.payment.entity.PaymentGateway;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentGatewayMapper extends BaseMapper<PaymentGateway> {
}
