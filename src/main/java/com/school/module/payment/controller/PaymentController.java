package com.school.module.payment.controller;

import com.school.common.annotation.NotTokenVerify;
import com.school.common.annotation.VerifyPaymentGateway;
import com.school.common.common.api.ApiResult;
import com.school.common.enums.PaymentGatewayEnum;
import com.school.common.utils.RequestUtil;
import com.school.module.payment.dto.PaymentDTO;
import com.school.module.payment.dto.PaymentNotifyDTO;
import com.school.module.payment.serice.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${module-path.payment}")
public class PaymentController {

    private static final String rechargeNotifyPrint = "success";

    private final PaymentService paymentService;

    /**
     * 金流付款
     *
     * @param paymentDto dto
     * @return vo
     */
    // todo 需要配置訂單處理
    @VerifyPaymentGateway
    @PostMapping("/payment")
    public ApiResult<?> payment(@Valid @RequestBody PaymentDTO paymentDto) {
        return ApiResult.success(paymentService.generatePayment(paymentDto));
    }


    /**
     * 金流回調
     */
    @NotTokenVerify
    @PostMapping(value = "/notify/{paymentGatewayCode}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public void notify(@PathVariable Integer paymentGatewayCode, HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        try {
            PrintWriter out = response.getWriter();
            PaymentGatewayEnum paymentGatewayEnum = PaymentGatewayEnum.convertPaymentGatewayEnum(paymentGatewayCode);
            if (paymentGatewayEnum == null || Objects.equals(paymentGatewayEnum, PaymentGatewayEnum.MANUAL)) {
                log.error("金流回調異常, PaymentGatewayCode:{}, 無此服務", paymentGatewayCode);
                out.write(rechargeNotifyPrint);
                out.flush();
                return;
            }

            String paymentText = paymentGatewayEnum.getText();
            String requestIp = RequestUtil.getRequestIp(request);
            log.info("[{}] request ip:{} 開始", paymentText, requestIp);

            Map<String, String> bodyMap = RequestUtil.getResMap(request);
            log.info("[{}] Parameter:[{}]", paymentText, bodyMap);
            if (bodyMap.isEmpty()) {
                log.error("[{}] 回調參數異常, 無任何回調參數", paymentText);
                out.write(rechargeNotifyPrint);
                out.flush();
                return;
            }

            Map<String, String> headerMap = RequestUtil.getHeaderMap(request);
            log.info("[{}] Header:[{}]", paymentText, headerMap);

            PaymentNotifyDTO paymentNotifyDto = new PaymentNotifyDTO();
            paymentNotifyDto.setPaymentGatewayEnum(paymentGatewayEnum);
            paymentNotifyDto.setRequestMap(bodyMap);
            paymentNotifyDto.setHeaderMap(headerMap);
            String notifyMessage = paymentService.notify(paymentNotifyDto);

            out.write(notifyMessage);
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("[{}] 金流回調, 系統發生異常", paymentGatewayCode, e);
        }

    }

}
