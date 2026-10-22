package com.school.module.payment.controller;

import com.school.common.annotation.NotTokenVerify;
import com.school.common.annotation.VerifyPaymentGateway;
import com.school.common.common.api.ApiResult;
import com.school.module.payment.dto.PaymentDTO;
import com.school.module.payment.handler.PaymentHandler;
import com.school.module.payment.serice.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${module-path.payment}")
public class PaymentController {

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


//    /**
//     * 綠界回調
//     */
//    @NotTokenVerify
//    @PostMapping(value = "/notify/ecpay", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
//    public ResponseEntity<String> notify(@RequestParam Map<String, String> fields) {
//        return ResponseEntity.status(HttpStatus.OK).body(paymentService.verifyNotification(fields));
//    }

}
