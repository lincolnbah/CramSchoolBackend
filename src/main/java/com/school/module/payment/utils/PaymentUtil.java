package com.school.module.payment.utils;

import com.school.common.common.api.CommonCode;
import com.school.common.common.exception.ApiException;
import com.school.common.enums.PaymentGatewayEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;

@Component
public class PaymentUtil {

    @Value("${domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String path;

    @Value("${module-path.payment}")
    private String module;

    public String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new ApiException(CommonCode.ENCODING_FAILED);
        }
    }

    // a-z排序 不排空值
    public String convertMapToQueryString(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=");
            if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
                sb.append(entry.getValue());
            }
        }
        return sb.toString();
    }

    // a-z排序 排空值
    public String convertMapToQueryStringIgnoreEmpty(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() == null || entry.getValue().toString().isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    // notify api
    public String returnNotifyUrl(PaymentGatewayEnum paymentGatewayEnum) {
        return domain + path + module + "/notify/" + paymentGatewayEnum.name().toLowerCase(Locale.ROOT);
    }

}
