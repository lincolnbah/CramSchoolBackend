package com.school.module.payment.serice.impl;

import com.school.common.common.api.CommonCode;
import com.school.common.common.exception.ApiException;
import com.school.common.utils.OrderUtil;
import com.school.common.utils.SHAUtil;
import com.school.module.payment.dto.PaymentDTO;
import com.school.module.payment.entity.PaymentGateway;
import com.school.module.payment.serice.PaymentGatewayService;
import com.school.module.payment.serice.PaymentService;
import com.school.module.payment.utils.PaymentUtil;
import com.school.module.payment.vo.PaymentNotificationVO;
import com.school.module.payment.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 綠界
 */
@Service
@RequiredArgsConstructor
public class ECPay implements PaymentService {

    private final PaymentGatewayService paymentGatewayService;

    @Override
    public PaymentVO createPayment(PaymentDTO request) {
        PaymentGateway paymentGateway = paymentGatewayService.findOne();
        String itemName = this.handlerItemName(request.getProductName());

        Map<String, Object> fields = new TreeMap<>();
        fields.put("MerchantID", paymentGateway.getMerchantId());
        fields.put("MerchantTradeNo", request.getOrderNo());
        fields.put("MerchantTradeDate", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        fields.put("PaymentType", "aio");
        fields.put("TotalAmount", Integer.toString(request.getAmount()));
        fields.put("TradeDesc", request.getDescription());
        fields.put("ItemName", itemName);
        fields.put("ReturnURL", paymentGateway.getNotifyUrl());
        fields.put("ChoosePayment", "ALL");
        fields.put("EncryptType", "1");
        fields.put("OrderResultURL", paymentGateway.getReturnUrl());

        String paramStr = PaymentUtil.convertMapToQueryString(fields);
        paramStr = "HashKey=" + paymentGateway.getHashKey() + "&" + paramStr + "&HashIV=" + paymentGateway.getHashIv();

        String encoded = PaymentUtil.encode(paramStr).toLowerCase(Locale.ROOT)
                .replace("%21", "!")
                .replace("%28", "(")
                .replace("%29", ")");
        fields.put("CheckMacValue", SHAUtil.sha256(encoded).toUpperCase(Locale.ROOT));

        PaymentVO paymentVo = new PaymentVO();
        paymentVo.setActionUrl(paymentGateway.getApiUrl());
        paymentVo.setFields(fields);
        return paymentVo;
    }

    @Override
    public PaymentNotificationVO verifyNotification(Map<String, String> input,
                                                    String expectedOrderNo, int expectedAmount) {
        return null;
    }

    private String handlerItemName(List<String> productNameList) {
        String itemName;
        if (productNameList.size() == 1) {
            itemName = productNameList.get(0);
        } else {
            itemName = String.join("#", productNameList);
        }
        if (itemName.length() >= 400) {
            throw new ApiException(CommonCode.EC_PAY_PRODUCT_NAME_TO_LONG);
        }
        return itemName;
    }
}
