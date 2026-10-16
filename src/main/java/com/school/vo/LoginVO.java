package com.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import java.time.Instant;

@Data
@AllArgsConstructor
@Schema(description = "登入成功資料")
public class LoginVO {
    @Schema(description = "簽章 JWT，後續放入 Authorization: Bearer <token>")
    private final String accessToken;

    @Schema(description = "Token 類型 Bearer")
    private final String tokenType;

    @Schema(description = "有效秒數，預設 900")
    private final long expiresIn;

    @Schema(description = "Token 到期時間")
    private final Instant expiresAt;

    @Schema(description = "目前角色 ID")
    private final Integer role;

}
