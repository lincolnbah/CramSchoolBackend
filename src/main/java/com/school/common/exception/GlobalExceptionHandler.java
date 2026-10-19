package com.school.common.exception;

import com.school.common.api.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public ApiResult<?> handle(ApiException e) {
        log.info("GlobalExceptionHandler ApiException", e);
        if (e.getErrorCode() != null) {
            return ApiResult.failed(e.getMessage(), e.getErrorCode().getCode());
        }
        return ApiResult.failed(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ApiResult<?> defaultHandleException(HttpServletRequest req, Exception e) {
        log.error("GlobalExceptionHandler unhandled exception,uri: {}", req.getRequestURI(), e);
        return ApiResult.failed();
    }

}
