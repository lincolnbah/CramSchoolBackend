package com.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PublicKeyVO {

    @Schema(description = "SPKI DER 格式的 RSA 公鑰，以標準 Base64 編碼")
    private final String publicKey;

    @Schema(description = "前端 Web Crypto 使用 RSA-OAEP，hash 使用 SHA-256")
    private final String algorithm = "RSA-OAEP-256";

    private final String keyFormat = "SPKI";

}
