package com.school.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecretKey jwtSigningKey(AuthProperties properties) {
        if (properties.getJwtSecret() == null || properties.getTokenTtlSeconds() <= 0
                || properties.getIssuer() == null || properties.getIssuer().trim().isEmpty()) {
            throw new IllegalStateException("請設定 JWT_SECRET、issuer 與正數的 Token 有效秒數");
        }
        byte[] bytes = Base64.getDecoder().decode(properties.getJwtSecret());
        if (bytes.length < 32) throw new IllegalStateException("JWT_SECRET 解碼後至少需要 32 bytes");
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(jwtSigningKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSigningKey, AuthProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
                .macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> required = jwt -> {
            try {
                Integer.parseInt(jwt.getSubject());
                Integer.parseInt(jwt.getClaim("role").toString());
                Instant now = Instant.now();
                if (jwt.getIssuedAt() != null && jwt.getExpiresAt() != null
                        && !jwt.getIssuedAt().isAfter(now)
                        && jwt.getExpiresAt().isAfter(now)
                        && jwt.getExpiresAt().isAfter(jwt.getIssuedAt())
                        && Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).getSeconds()
                        <= properties.getTokenTtlSeconds()) return OAuth2TokenValidatorResult.success();
            } catch (RuntimeException ignored) { }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token 無效或已過期", null));
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<Jwt>(
                new JwtIssuerValidator(properties.getIssuer()), new JwtTimestampValidator(Duration.ZERO), required));
        return decoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiAuthorizationManager authorization,
                                                   SecurityErrorHandler errors, AuthProperties properties) throws Exception {
        if (properties.isRequireHttps()) http.requiresChannel().anyRequest().requiresSecure();
        http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().requestCache().disable().formLogin().disable().httpBasic().disable().logout().disable()
                .authorizeHttpRequests(authorize -> authorize
                        .antMatchers(HttpMethod.POST, "/auth/login", "/auth/public-key", "/auth/register").permitAll()
                        .anyRequest().access(authorization))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(errors).accessDeniedHandler(errors))
                .oauth2ResourceServer(resource -> resource.jwt().and()
                        .authenticationEntryPoint(errors).accessDeniedHandler(errors));
        return http.build();
    }
}
