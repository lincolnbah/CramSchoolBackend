package com.school.module.payment.utils;

import com.school.common.common.api.CommonCode;
import com.school.common.common.exception.ApiException;

import java.net.URLEncoder;
import java.util.Map;

public class PaymentUtil {

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new ApiException(CommonCode.ENCODING_FAILED);
        }
    }

    // a-z排序 不排空值
    public static String convertMapToQueryString(Map<?, ?> map) {
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
    public static String convertMapToQueryStringIgnoreEmpty(Map<?, ?> map) {
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

}
