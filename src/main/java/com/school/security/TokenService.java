package com.school.security;

import cn.hutool.core.util.IdUtil;
import com.school.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtEncoder encoder;
    private final AuthProperties properties;

    public LoginVO issue(Integer accountId, Integer roleId) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(properties.getTokenTtlSeconds());
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(properties.getIssuer())
                .subject(accountId.toString()).issuedAt(now).expiresAt(expires)
                .id(IdUtil.simpleUUID()).claim("role", roleId).build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginVO(token, "Bearer", properties.getTokenTtlSeconds(), expires, roleId);
    }
}
