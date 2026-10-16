package com.school.common.api;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import com.school.aspect.filter.TraceIdFilter;

import java.time.LocalDateTime;

@Slf4j
public class ApiResult<T> {
    /**
     * 狀態碼
     */
    @Schema(description = "錯誤代碼,請查閱錯誤代碼附錄")
    private long code;
    /**
     * 提示訊息
     */
    @Schema(description = "錯誤描述,請查閱錯誤代碼附錄")
    private String message;
    /**
     * 資料封裝
     */
    @Schema(description = "回傳資料")
    private T data;

    @Schema(description = "請求 UUID 追蹤碼，與日誌及 X-Trace-Id 回應標頭一致")
    private final String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);

    public String getTraceId() {
        return traceId;
    }


    @Schema(description = "伺服器目前時間")
    private LocalDateTime serviceTimeNow;

    protected ApiResult() {
    }

    protected ApiResult(long code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.serviceTimeNow = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return this.code == CommonCode.SUCCEED.getCode();
    }

    /**
     * 成功回傳結果
     */
    public static <T> ApiResult<T> success() {
        return new ApiResult<T>(CommonCode.SUCCEED.getCode(), CommonCode.SUCCEED.getMessage(), null);
    }

    /**
     * 成功回傳結果
     *
     * @param data 取得的資料
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<T>(CommonCode.SUCCEED.getCode(), CommonCode.SUCCEED.getMessage(), data);
    }

    /**
     * 成功回傳結果
     *
     * @param data    取得的資料
     * @param message 提示訊息
     */
    public static <T> ApiResult<T> success(T data, String message) {
        return new ApiResult<T>(CommonCode.SUCCEED.getCode(), message, data);
    }

    /**
     * 失敗回傳結果
     *
     * @param errorCode 錯誤碼
     */
    public static <T> ApiResult<T> failed(IErrorCode errorCode) {
        return new ApiResult<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失敗回傳結果
     *
     * @param errorCode 錯誤碼
     */
    public static <T> ApiResult<T> failedWithData(IErrorCode errorCode, T t) {
        return new ApiResult<>(errorCode.getCode(), errorCode.getMessage(), t);
    }

    /**
     * 失敗回傳結果
     *
     * @param errorCode 錯誤碼
     * @param message   錯誤訊息
     */
    public static <T> ApiResult<T> failed(IErrorCode errorCode, String message) {
        return new ApiResult<T>(errorCode.getCode(), message, null);
    }

    /**
     * 失敗回傳結果
     *
     * @param message 提示訊息
     */
    public static <T> ApiResult<T> failed(String message) {
        return new ApiResult<T>(CommonCode.FAILED.getCode(), message, null);
    }

    /**
     * 失敗回傳結果
     */
    public static <T> ApiResult<T> failed() {
        return failed(CommonCode.FAILED);
    }

    /**
     * 參數驗證失敗回傳結果
     */
    public static <T> ApiResult<T> validateFailed() {
        return failed(CommonCode.PARAM_INVALID);
    }

    /**
     * 參數驗證失敗回傳結果
     *
     * @param message 提示訊息
     */
    public static <T> ApiResult<T> validateFailed(String message) {
        return new ApiResult<T>(CommonCode.PARAM_INVALID.getCode(), message, null);
    }


    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
