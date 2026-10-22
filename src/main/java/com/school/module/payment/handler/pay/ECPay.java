package com.school.module.payment.handler.pay;

import com.school.common.common.api.CommonCode;
import com.school.common.common.exception.ApiException;
import com.school.common.utils.SHAUtil;
import com.school.module.payment.dto.PaymentDTO;
import com.school.module.payment.entity.PaymentGateway;
import com.school.common.enums.PaymentGatewayEnum;
import com.school.module.payment.serice.PaymentGatewayService;
import com.school.module.payment.handler.PaymentHandler;
import com.school.module.payment.utils.PaymentUtil;
import com.school.module.payment.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 綠界
 */
@Service
@RequiredArgsConstructor
public class ECPay implements PaymentHandler {

    private static final String LOG_NAME = "綠界";

    private static final String NOTIFY_KEY = "1|OK";

    private final PaymentGatewayService paymentGatewayService;

    private final PaymentUtil paymentUtil;

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
        fields.put("ReturnURL", paymentUtil.returnNotifyUrl(PaymentGatewayEnum.ECPAY));
        fields.put("ChoosePayment", "ALL");
        fields.put("EncryptType", "1");
        fields.put("ClientBackURL", paymentGateway.getReturnUrl());

        String paramStr = paymentUtil.convertMapToQueryString(fields);
        paramStr = "HashKey=" + paymentGateway.getHashKey() + "&" + paramStr + "&HashIV=" + paymentGateway.getHashIv();

        String encoded = paymentUtil.encode(paramStr).toLowerCase(Locale.ROOT)
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
    public String verifyNotification(Map<String, String> fields) {
        if (fields == null || fields.isEmpty()) {
            return "0|Invalid notification";
        }
        // 保留原始欄位值，不 trim 或再次 URL decode；包含空值的欄位也要參與驗簽。
        fields = new HashMap<>(fields);
        String mac = fields.get("CheckMacValue");
        if (mac == null || !mac.matches("[0-9A-Fa-f]{64}")) {
            return "0|Invalid CheckMacValue";
        }
        PaymentGateway gateway = paymentGatewayService.findOne();
        if (gateway == null || StringUtils.isBlank(gateway.getMerchantId())
                || StringUtils.isBlank(gateway.getHashKey()) || StringUtils.isBlank(gateway.getHashIv())) {
            return "0|Payment gateway not configured";
        }
        Map<String, String> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String key = entry.getKey();
            if (key == null || !names.add(key)) {
                return "0|Invalid notification fields";
            }
            if (!"CheckMacValue".equalsIgnoreCase(key)) {
                sorted.put(key, entry.getValue());
            }
        }
        String raw = "HashKey=" + gateway.getHashKey() + "&"
                + paymentUtil.convertMapToQueryString(sorted) + "&HashIV=" + gateway.getHashIv();
        String encoded = paymentUtil.encode(raw).toLowerCase(Locale.ROOT)
                .replace("%2d", "-").replace("%5f", "_").replace("%2e", ".")
                .replace("%21", "!").replace("%2a", "*")
                .replace("%28", "(").replace("%29", ")");
        String expected = SHAUtil.sha256(encoded).toUpperCase(Locale.ROOT);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                mac.toUpperCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII))) {
            return "0|Invalid CheckMacValue";
        }
        if (!gateway.getMerchantId().equals(fields.get("MerchantID"))) {
            return "0|Invalid MerchantID";
        }
        if (StringUtils.isBlank(fields.get("MerchantTradeNo")) || StringUtils.isBlank(fields.get("TradeNo"))
                || StringUtils.isBlank(fields.get("RtnMsg"))) {
            return "0|Missing notification fields";
        }
        String rtnCode = fields.get("RtnCode");
        String amount = fields.get("TradeAmt");
        String simulatePaid = fields.get("SimulatePaid");
        if (rtnCode == null || !rtnCode.matches("[0-9]{1,10}")
                || amount == null || !amount.matches("[0-9]{1,10}")
                || !("0".equals(simulatePaid) || "1".equals(simulatePaid))) {
            return "0|Invalid notification fields";
        }
        long tradeAmount = Long.parseLong(amount);
        if (tradeAmount <= 0 || tradeAmount > Integer.MAX_VALUE) {
            return "0|Invalid TradeAmt";
        }
        // 模擬付款只確認接收，不能更新已付款或出貨。
        if ("1".equals(simulatePaid)) {
            return "1|OK";
        }
        if (!"1".equals(rtnCode)) {
            // TODO 保存 RtnCode / RtnMsg，不能覆蓋已付款狀態；保存完成後回 1|OK。
            return "0|Payment failure recording not configured";
        }
        // TODO 依 MerchantTradeNo 查本機訂單，核對金流商、商戶及 TradeAmt 與應付金額。
        // 在交易內核對 TradeNo 並冪等更新，避免重送通知重複入帳；完成持久化後回 1|OK。
        return "0|Order processing not configured";
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
