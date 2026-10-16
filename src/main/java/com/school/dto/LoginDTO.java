package com.school.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "登入請求，必須透過 HTTPS 傳送")
public class LoginDTO {
    @NotBlank(message = "帳號不可為空")
    @Size(max = 50, message = "帳號最多 50 字元")
    @Schema(description = "登入帳號")
    private String account;

    @NotBlank(message = "密碼不可為空")
    @Size(max = 344, message = "RSA 密文最多 344 個 Base64 字元")
    @Schema(description = "使用帳號公鑰以 RSA-OAEP SHA-256 加密後的 Base64 密碼，禁止傳明文", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Override
    public String toString() { return "LoginRequest{credentials=REDACTED}"; }
}
