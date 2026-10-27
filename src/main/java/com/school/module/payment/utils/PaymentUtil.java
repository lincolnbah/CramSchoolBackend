package com.school.module.payment.utils;

import com.school.common.common.api.CommonCode;
import com.school.common.common.exception.ApiException;
import com.school.common.enums.PaymentGatewayEnum;
import com.school.module.order.enums.StatusEnum;
import com.school.module.payment.vo.PaymentNotifyVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
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
        return domain + path + module + "/notify/" + paymentGatewayEnum.getCode();
    }

    /**
     * 回調驗簽失敗
     *
     * @param statusEnum enum
     * @param errorMsg   錯誤訊息
     * @return vo
     */
    public static PaymentNotifyVO paySignError(StatusEnum statusEnum, String errorMsg) {
        return new PaymentNotifyVO()
                .setStatusEnum(statusEnum)
                .setSignError(true)
                .setStatusError(false)
                .setOrderMessage(statusEnum.getText().concat("  訂單狀態/描述: ").concat(errorMsg));
    }

    /**
     * 回調狀態失敗
     *
     * @param statusEnum enum
     * @param errorMsg   錯誤訊息
     * @return vo
     */
    public static PaymentNotifyVO payStatusError(StatusEnum statusEnum, String errorMsg) {
        return new PaymentNotifyVO()
                .setStatusEnum(statusEnum)
                .setSignError(false)
                .setStatusError(true)
                .setOrderMessage(statusEnum.getText().concat("  訂單狀態/描述: ").concat(errorMsg));
    }

}
