package com.school.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class PublicKeyDTO {

    @NotBlank(message = "帳號不可為空")
    @Size(max = 50, message = "帳號最多 50 字元")
    @Schema(description = "需要查詢登入公鑰的帳號")
    private String account;

}
