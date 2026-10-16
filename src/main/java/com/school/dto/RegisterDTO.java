package com.school.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "註冊資料，尚未有專屬公鑰，原始密碼必須以 HTTPS 傳送")
public class RegisterDTO {
    @NotBlank(message = "帳號不可為空")
    @Size(max = 50)
    private String account;
    @NotBlank(message = "姓名不可為空")
    @Size(max = 50)
    private String name;
    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, max = 72, message = "密碼需為 8 到 72 字元，UTF-8 不可超過 72 bytes")
    @ToString.Exclude
    @Schema(description = "初始密碼，後端只保存 BCrypt 雜湊", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;
}
