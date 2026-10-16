package com.school.common.exception;

import com.school.common.api.ApiResult;
import com.school.common.api.CommonCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResult<?>> handleAuthenticationException(AuthenticationException exception) {
        return ResponseEntity.status(401).body(ApiResult.failed(CommonCode.LOGIN_FAILED));
    }


    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public ApiResult<?> handle(ApiException e) {
        log.info("GlobalExceptionHandler ApiException", e);
        if (e.getErrorCode() != null) {
            return ApiResult.failed(e.getErrorCode(), e.getMessage());
        }
        return ApiResult.failed(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ApiResult<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.info("GlobalExceptionHandler IllegalArgumentException", e);
        return ApiResult.validateFailed(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResult<?> handleValidException(MethodArgumentNotValidException e) {
        log.info("GlobalExceptionHandler MethodArgumentNotValidException", e);
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField() + fieldError.getDefaultMessage();
            }
        }
        return ApiResult.validateFailed(message);
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ApiResult<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.info("GlobalExceptionHandler MethodArgumentTypeMismatchException", e);
        return ApiResult.validateFailed(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ApiResult<?> defaultHandleException(HttpServletRequest req, Exception e) {
        log.error("GlobalExceptionHandler unhandled exception,uri: {}", req.getRequestURI(), e);
        return ApiResult.failed();
    }

}
