package com.school.common.api;


import lombok.Getter;

/**
 * @author none
 */

@Getter
public enum CommonCode implements IErrorCode {

    // 成功
    SUCCEED(200, "Succeed"),
    // 失敗
    FAILED(10001, "Failed"),
    // 參數錯誤
    PARAM_INVALID(10002, "Param invalid"),
    // 頻繁請求
    FREQUENT_REQUESTS(10003, "Frequent requests"),
    // 身份錯誤
    ACCESS_DENY(10004, "權限不足"),
    UNAUTHORIZED(10005, "尚未登入，或 Token 無效、已過期"),
    LOGIN_FAILED(10006, "帳號或密碼錯誤"),


    ;


    private final int code;
    private final String message;

    CommonCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
