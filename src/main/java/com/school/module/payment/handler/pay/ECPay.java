package com.school.module.payment.handler.pay;

import com.school.common.common.api.CommonCode;
import com.school.common.common.exception.ApiException;
import com.school.common.enums.PaymentGatewayEnum;
import com.school.common.utils.SHAUtil;
import com.school.module.order.enums.StatusEnum;
import com.school.module.payment.dto.PaymentDTO;
import com.school.module.payment.entity.PaymentGateway;
import com.school.module.payment.handler.PaymentHandler;
import com.school.module.payment.utils.PaymentUtil;
import com.school.module.payment.vo.PaymentNotifyVO;
import com.school.module.payment.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;

/**
 * 綠界
 */
@Slf4j
@Service("ecpay")
@RequiredArgsConstructor
public class ECPay implements PaymentHandler {

    private static final String LOG_NAME = "綠界";

    private static final String CALLBACK_SUCCESS_CODE = "1";

    private static final String NOTIFY_KEY = "1|OK";

    private static final String NOTIFY_ORDER_KEY = "MerchantTradeNo";

    private final PaymentUtil paymentUtil;

    @Override
    public String orderKey() {
        return NOTIFY_ORDER_KEY;
    }

    @Override
    public String notifyPrint() {
        return NOTIFY_KEY;
    }

    @Override
    public PaymentVO createPayment(PaymentDTO request) {
        String orderNo = request.getOrderNo();
        PaymentGateway paymentGateway = request.getPaymentGateway();
        String itemName = this.handlerItemName(request.getProductName());

        Map<String, Object> fields = new TreeMap<>();
        fields.put("MerchantID", paymentGateway.getMerchantId());
        fields.put("MerchantTradeNo", orderNo);
        fields.put("MerchantTradeDate", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        fields.put("PaymentType", "aio");
        fields.put("TotalAmount", Integer.toString(request.getAmount()));
        fields.put("TradeDesc", request.getDescription());
        fields.put("ItemName", itemName);
        fields.put("ReturnURL", paymentUtil.returnNotifyUrl(PaymentGatewayEnum.ECPAY));
        fields.put("ChoosePayment", "ALL");
        fields.put("EncryptType", "1");
        fields.put("ClientBackURL", paymentGateway.getReturnUrl());

        String paramStr = paymentUtil.convertMapToQueryString(fields);

        log.info("[{}][{}] 產生簽名字串加密前 [{}] ", LOG_NAME, orderNo, "HashKey={HashKey}&" + paramStr + "&HashIV={HashIV}");

        paramStr = "HashKey=" + paymentGateway.getHashKey() + "&" + paramStr + "&HashIV=" + paymentGateway.getHashIv();

        String encoded = paymentUtil.encode(paramStr).toLowerCase(Locale.ROOT)
                .replace("%2d", "-").replace("%5f", "_").replace("%2e", ".")
                .replace("%21", "!").replace("%2a", "*")
                .replace("%28", "(").replace("%29", ")");
        fields.put("CheckMacValue", SHAUtil.sha256(encoded).toUpperCase(Locale.ROOT));

        log.info("[{}][{}] request params = {}", LOG_NAME, orderNo, fields);

        PaymentVO paymentVo = new PaymentVO();
        paymentVo.setActionUrl(paymentGateway.getApiUrl());
        paymentVo.setFields(fields);
        return paymentVo;
    }

    @Override
    public PaymentNotifyVO verifyNotification(Map<String, String> paramsMap, Map<String, String> headerMap, PaymentGateway paymentGateway) {
        try {
            String orderNo = paramsMap.get(NOTIFY_ORDER_KEY);
            Map<String, String> resMap = new TreeMap<>(paramsMap);
            String checkMacValue = resMap.remove("CheckMacValue");

            String raw = "HashKey=" + paymentGateway.getHashKey() + "&"
                    + paymentUtil.convertMapToQueryString(resMap) + "&HashIV=" + paymentGateway.getHashIv();
            String encoded = paymentUtil.encode(raw).toLowerCase(Locale.ROOT)
                    .replace("%2d", "-").replace("%5f", "_").replace("%2e", ".")
                    .replace("%21", "!").replace("%2a", "*")
                    .replace("%28", "(").replace("%29", ")");
            String checkMacValueSign = SHAUtil.sha256(encoded).toUpperCase(Locale.ROOT);
            if (!Objects.equals(checkMacValue, checkMacValueSign)) {
                log.error("[{}][{}][金流回調發生錯誤] 簽名驗證失敗", LOG_NAME, orderNo);
                return PaymentUtil.paySignError(StatusEnum.SIGN_CHECK_ERROR, "金鑰驗證失敗");
            }

            String status = paramsMap.get("RtnCode");
            log.info("[{}][{}] 訂單狀態 = {}", LOG_NAME, orderNo, status);

            if (CALLBACK_SUCCESS_CODE.equals(status)) {
                LocalDateTime paymentTime = LocalDateTime.parse(paramsMap.get("PaymentDate"),
                        DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT));

                PaymentNotifyVO paymentNotifyVo = new PaymentNotifyVO();
                paymentNotifyVo.setThirdOrderNo(paramsMap.get("TradeNo"));
                paymentNotifyVo.setAmount(new BigDecimal(paramsMap.get("TradeAmt")));
                paymentNotifyVo.setFee(new BigDecimal(paramsMap.get("PaymentTypeChargeFee")));
                paymentNotifyVo.setPaymentType(paramsMap.get("PaymentType"));
                paymentNotifyVo.setPaymentTime(paymentTime);
                paymentNotifyVo.setOrderMessage(paramsMap.get("RtnMsg"));
                if (Objects.equals(paramsMap.get("SimulatePaid"), "1")) {
                    paymentNotifyVo.setSimulatePaid(Boolean.TRUE);
                }
                log.info("[{}][{}] pay = {}", LOG_NAME, orderNo, paymentNotifyVo);
                return paymentNotifyVo;
            }

            return PaymentUtil.payStatusError(StatusEnum.NOTIFY_FAIL, paramsMap.get("RtnMsg"));
        } catch (Exception e) {
            log.error("[{}][金流發生錯誤]", LOG_NAME, e);
            PaymentNotifyVO paymentNotifyVo = new PaymentNotifyVO();
            paymentNotifyVo.setStatusEnum(StatusEnum.SERVER_ERROR);
            paymentNotifyVo.setOrderMessage("系統發生錯誤");
            return paymentNotifyVo;
        }
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
