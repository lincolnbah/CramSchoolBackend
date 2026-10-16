package com.school.common.response;

import com.school.common.api.CommonCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.slf4j.MDC;
import com.school.aspect.filter.TraceIdFilter;

@Getter
@Schema(description = "統一 API 回應")
public class ApiResponse<T> {
    @Schema(description = "回應碼，200 表示成功，其餘表示錯誤")
    private final int code;
    @Schema(description = "回應訊息")
    private final String message;
    @Schema(description = "回應資料，失敗或無資料時為 null")
    private final T data;

    @Schema(description = "請求 UUID 追蹤碼，與日誌及 X-Trace-Id 回應標頭一致")
    private final String traceId;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(CommonCode.SUCCEED.getCode(), "成功", data);
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
