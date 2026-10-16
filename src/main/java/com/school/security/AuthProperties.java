package com.school.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "security.auth")
public class AuthProperties {
    /** Base64 編碼的隨機金鑰，解碼後至少 32 bytes，禁止放入版本控制。 */
    private String jwtSecret;
    /** 僅後端持有，用於加密資料庫中的 RSA 私鑰，不可與 JWT 金鑰共用。 */
    private String keyEncryptionSecret;
    private long tokenTtlSeconds = 900;
    private String issuer = "cram-school";
    private boolean requireHttps = true;
}
