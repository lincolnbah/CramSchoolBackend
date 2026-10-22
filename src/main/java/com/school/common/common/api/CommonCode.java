package com.school.common.common.api;


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
    // 無效請求
    INVALID_REQUEST(10005, "Invalid request"),
    // API驗證異常
    API_VERIFY_ERROR(10006, "Verify api error"),
    // 功能未開放
    FEATURE_NOT_YET_AVAILABLE(10007, "Feature not yet available"),

    /**
     * 金流
     */
    // 綠界商品名稱不能超過400字, 需要減少商品項目在下單
    EC_PAY_PRODUCT_NAME_TO_LONG(20001, "Please remove some items from your cart before checking out."),
    // 金流未實作
    PAYMENT_SERVER_ERROR(20002, "payment server error"),

    /**
     * 訂單
     */
    // 訂單建立失敗, 請重新操作
    ORDER_CREATION_FAILED(30001, "Order creation failed. Please try again."),


    /**
     * 加解密
     */
    // 待加密資料過長
    PLAINTEXT_EXCEEDS_THE_LIMIT(70000, "Plaintext exceeds the limit"),
    // 解密失敗, 密鑰錯誤
    CIPHERTEXT_DOES_NOT_MATCH(70001, "Ciphertext does not match"),
    // 解密異常
    DECRYPT_ERROR(70002, "Decrypt error"),
    // 生成RSA公私鑰失敗
    CREATE_RSA_KEY_ERROR(70003, "Failed to create key"),
    // 加密失敗
    ENCRYPTION_FAILED(70004, "Encryption failed"),
    // 編碼失敗
    ENCODING_FAILED(70005, "Encoding failed"),


    /**
     * 帳號
     */
    // 停權
    ACCOUNT_SUSPENDED(80000, "account suspended"),
    // 無權限
    ACCOUNT_NO_PERMISSION(80001, "No permission"),
    // 帳號異常
    ACCOUNT_ABNORMAL(80002, "account abnormal"),
    // 帳號或密碼不存在
    ACCOUNT_PASSWORD_NOT_EXIST(80003, "Account or password does not exist"),
    // 登錄失敗
    LOGIN_FAILED(80004, "Login failed; please log in again."),
    // 重複登錄
    DUPLICATE_LOGIN(80005, "Duplicate login"),
    // 二次密碼不正確
    INCORRECT_PASSWORD_ENTERED_TWICE(80006, "Incorrect password entered twice"),
    // 註冊失敗
    REGISTRATION_FAILED(80006, "Registration failed; please try again."),


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
