package com.school.common.common.exception;

import com.school.common.common.api.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public ApiResult<?> handleApiException(ApiException e) {
        log.info("handleApiException ApiException, msg:{}", e.getMessage(), e);
        if (e.getErrorCode() != null) {
            return ApiResult.failed(e.getMessage(), e.getErrorCode().getCode());
        }
        return ApiResult.failed(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<?> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("; "));
        log.error("handleValidationException msg: {}", message);
        return ApiResult.validateFailed(message);
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ApiResult<?> defaultHandleException(HttpServletRequest req, Exception e) {
        log.error("GlobalExceptionHandler unhandled exception,uri: {}", req.getRequestURI(), e);
        return ApiResult.failed();
    }

}
