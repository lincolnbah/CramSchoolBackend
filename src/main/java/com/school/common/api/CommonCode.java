package com.school.common.api;


import lombok.Getter;

/**
 * @author none
 */

@Getter
public enum CommonCode implements IErrorCode {

    /**
     * 一般
     */
    // 成功
    SUCCEED(200, "Succeed"),
    // 失敗
    FAILED(10001, "Failed"),
    // 參數錯誤
    PARAM_INVALID(10002, "Param invalid"),
    // 頻繁請求
    FREQUENT_REQUESTS(10003, "Frequent requests"),
    // api header錯誤
    API_HEADER_ERROR(10004, "api header error"),

    /**
     * 帳號
     */
    // 停權
    ACCOUNT_SUSPENDED(80000, "account suspended"),
    // 無權限
    ACCOUNT_NO_PERMISSION(80001, "No permission"),
    ACCOUNT_ABNORMAL(80002, "account abnormal"),

    /**
     * token
     */
    // 驗證失敗
    TOKEN_ERROR(90000, "token verify error"),
    // 缺少token
    LACK_TOKEN(90001, "Lack token"),
    // token格式錯誤
    TOKEN_FORMAT_ERROR(90002, "token format error"),
    // token過期
    TOKEN_EXPIRED(90003, "token expired"),
    // 無效token
    SIGN_INVALID(90004, "token signature is invalid"),
    // 錯誤token
    TOKEN_FALSIFY(90005, "token falsify"),


    ;


    private final int code;
    private final String message;

    CommonCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
