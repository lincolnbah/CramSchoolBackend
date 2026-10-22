package com.school.module.payment.serice.impl;

import com.school.module.payment.entity.PaymentGateway;
import com.school.module.payment.handler.pay.ECPay;
import com.school.module.payment.serice.PaymentGatewayService;
import com.school.module.payment.utils.PaymentUtil;
import com.school.common.utils.SHAUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ECPayNotificationTest {
    private PaymentGateway gateway;
    private ECPay service;

    @BeforeEach
    void setup() {
        gateway = new PaymentGateway();
        gateway.setMerchantId("3002607");
        gateway.setHashKey("pwFHCqoQZGmho4w6");
        gateway.setHashIv("EkRm7iFT261dpevs");
        PaymentGatewayService configuration = mock(PaymentGatewayService.class);
        when(configuration.findOne()).thenReturn(gateway);
        service = new ECPay(configuration, new PaymentUtil());
    }

    private Map<String, String> notification() {
        Map<String, String> fields = new HashMap<>();
        fields.put("MerchantID", "3002607");
        fields.put("MerchantTradeNo", "ORDER123");
        fields.put("TradeNo", "2601010000001234");
        fields.put("TradeAmt", "100");
        fields.put("RtnCode", "1");
        fields.put("RtnMsg", "交易成功 !()*-_. +%");
        fields.put("SimulatePaid", "0");
        fields.put("CustomField1", "");
        return fields;
    }

    private Map<String, String> signed(Map<String, String> fields) {
        Map<String, String> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(fields);
        sorted.remove("CheckMacValue");
        PaymentUtil util = new PaymentUtil();
        String raw = "HashKey=" + gateway.getHashKey() + "&"
                + util.convertMapToQueryString(sorted) + "&HashIV=" + gateway.getHashIv();
        String encoded = util.encode(raw).toLowerCase(Locale.ROOT)
                .replace("%21", "!").replace("%28", "(").replace("%29", ")");
        fields.put("CheckMacValue", SHAUtil.sha256(encoded).toUpperCase(Locale.ROOT));
        return fields;
    }

    @Test
    void matchesOfficialChecksumExample() {
        Map<String, String> fields = new HashMap<>();
        fields.put("TradeDesc", "促銷方案");
        fields.put("PaymentType", "aio");
        fields.put("MerchantTradeDate", "2023/03/12 15:30:23");
        fields.put("MerchantTradeNo", "ecpay20230312153023");
        fields.put("MerchantID", "3002607");
        fields.put("ReturnURL", "https://www.ecpay.com.tw/receive.php");
        fields.put("ItemName", "Apple iphone 15");
        fields.put("TotalAmount", "30000");
        fields.put("ChoosePayment", "ALL");
        fields.put("EncryptType", "1");
        fields.put("CheckMacValue", "6C51C9E6888DE861FD62FB1DD17029FC742634498FD813DC43D4243B5685B840");
        // 官方付款請求範例通過驗簽後，因缺少通知專屬欄位而被拒絕。
        assertEquals("0|Missing notification fields", service.verifyNotification(fields));
    }

    @Test
    void validRealPaymentWaitsForOrderPersistenceAndDoesNotMutateInput() {
        Map<String, String> fields = signed(notification());
        Map<String, String> original = new HashMap<>(fields);
        assertEquals("0|Order processing not configured", service.verifyNotification(fields));
        assertEquals(original, fields);
        fields.put("CheckMacValue", fields.get("CheckMacValue").toLowerCase(Locale.ROOT));
        assertEquals("0|Order processing not configured", service.verifyNotification(fields));
    }

    @Test
    void rejectsTamperedAmountStatusAndAdditionalFields() {
        for (String key : Arrays.asList("TradeAmt", "RtnCode", "SimulatePaid", "CustomField1", "Extra")) {
            Map<String, String> fields = signed(notification());
            fields.put(key, "2");
            assertEquals("0|Invalid CheckMacValue", service.verifyNotification(fields));
        }
    }

    @Test
    void rejectsOtherMerchantEvenWithValidSignature() {
        Map<String, String> fields = notification();
        fields.put("MerchantID", "OTHER");
        assertEquals("0|Invalid MerchantID", service.verifyNotification(signed(fields)));
    }

    @Test
    void simulatedPaymentIsAcknowledgedWithoutProcessingRealOrder() {
        Map<String, String> fields = notification();
        fields.put("SimulatePaid", "1");
        assertEquals("1|OK", service.verifyNotification(signed(fields)));
    }

    @Test
    void failedPaymentWaitsForFailureRecording() {
        Map<String, String> fields = notification();
        fields.put("RtnCode", "10100058");
        assertEquals("0|Payment failure recording not configured", service.verifyNotification(signed(fields)));
    }

    @Test
    void rejectsMalformedSignedFields() {
        for (String amount : Arrays.asList("0", "-1", "1.5", "2147483648", "999999999999999999999")) {
            Map<String, String> fields = notification();
            fields.put("TradeAmt", amount);
            assertTrue(service.verifyNotification(signed(fields)).startsWith("0|"));
        }
        for (String key : Arrays.asList("MerchantTradeNo", "TradeNo", "RtnMsg", "RtnCode", "SimulatePaid")) {
            Map<String, String> fields = notification();
            fields.remove(key);
            assertTrue(service.verifyNotification(signed(fields)).startsWith("0|"));
        }
    }

    @Test
    void rejectsEmptyBadMacAndAmbiguousFieldNames() {
        assertTrue(service.verifyNotification(null).startsWith("0|"));
        assertTrue(service.verifyNotification(Collections.emptyMap()).startsWith("0|"));
        Map<String, String> fields = notification();
        fields.put("CheckMacValue", "bad");
        assertEquals("0|Invalid CheckMacValue", service.verifyNotification(fields));
        signed(fields);
        fields.put("merchantid", "3002607");
        assertEquals("0|Invalid notification fields", service.verifyNotification(fields));
        fields.remove("merchantid");
        fields.put("checkmacvalue", fields.get("CheckMacValue"));
        assertEquals("0|Invalid notification fields", service.verifyNotification(fields));
    }

    @Test
    void rejectsMissingConfiguration() {
        Map<String, String> fields = signed(notification());
        gateway.setHashKey(null);
        assertEquals("0|Payment gateway not configured", service.verifyNotification(fields));
    }
}
