package com.school.security;

import com.school.common.api.ApiResult;
import com.school.common.api.CommonCode;
import com.school.entity.Account;
import com.school.entity.Api;
import com.school.service.AccountService;
import com.school.service.ApiService;
import com.school.support.RsaTestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"security.auth.jwt-secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "security.auth.key-encryption-secret=YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODk="})
@AutoConfigureMockMvc
@Import(AuthSecurityTest.Endpoints.class)
class AuthSecurityTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder encoder;
    @Autowired TokenService tokens;
    @Autowired JwtDecoder decoder;
    @Autowired JwtEncoder jwtEncoder;
    @Autowired AccountKeyService keys;
    private AccountKeyService.KeyMaterial material;
    @MockBean AccountService accounts;
    @MockBean ApiService permissions;

    @TestConfiguration
    static class Endpoints {
        @Bean TestController testController() { return new TestController(); }
    }
    @RestController
    static class TestController {
        @GetMapping("/test/items") ApiResult<String> getItems() { return ApiResult.success("ok"); }
        @PostMapping("/test/items") ApiResult<String> postItems() { return ApiResult.success("created"); }
        @GetMapping("/test/failure") ApiResult<Void> failure() { throw new IllegalStateException("private details"); }
    }

    @BeforeEach void setup() {
        material = keys.generate();
        Account account = new Account();
        account.setId(10); account.setAccount("tester"); account.setRole(2);
        account.setPassword(encoder.encode("correct-password"));
        account.setPublicKey(material.getPublicKey());
        account.setPrivateKey(material.getEncryptedPrivateKey());
        when(accounts.findLoginAccounts("tester")).thenReturn(Collections.singletonList(account));
        when(accounts.findCurrentRole(10)).thenReturn(2);
        Api api = new Api(); api.setId(1); api.setPath("/test/items"); api.setMethod(1);
        when(permissions.findApis(2, 1)).thenReturn(Collections.singletonList(api));
        when(permissions.findApis(2, 2)).thenReturn(Collections.emptyList());
    }

    @Test void loginIssuesRoleAndFifteenMinuteSignedToken() throws Exception {
        MvcResult result = mvc.perform(post("/auth/login").secure(true).contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("tester", "correct-password")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.expiresIn").value(900)).andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsString());
        Jwt jwt = decoder.decode(body.path("data").path("accessToken").asText());
        assertEquals("10", jwt.getSubject());
        assertEquals("2", jwt.getClaim("role").toString());
        assertEquals(900, jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond());
        assertTrace(result);
    }

    @Test void wrongOrUnknownCredentialsReturnSameError() throws Exception {
        for (String account : Arrays.asList("tester", "unknown")) {
            MvcResult result = mvc.perform(post("/auth/login").secure(true).contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody(account, "wrong")))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value(10006)).andReturn();
            assertTrace(result);
        }
    }

    @Test void noTokenAndTamperedTokenReturn401() throws Exception {
        assertTrace(mvc.perform(get("/test/items").secure(true)).andExpect(status().isUnauthorized()).andReturn());
        String token = tokens.issue(10, 2).getAccessToken();
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);
        parts[1] = Base64.getUrlEncoder().withoutPadding().encodeToString(
                payload.replace("\"role\":2", "\"role\":1").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertTrace(mvc.perform(get("/test/items").secure(true).header("Authorization", "Bearer " + String.join(".", parts)))
                .andExpect(status().isUnauthorized()).andReturn());
    }

    @Test void expiredWrongIssuerAndMissingClaimsAreRejected() throws Exception {
        Instant now = Instant.now();
        for (JwtClaimsSet claims : Arrays.asList(
                JwtClaimsSet.builder().issuer("cram-school").subject("10").claim("role", 2)
                        .issuedAt(now.minusSeconds(901)).expiresAt(now.minusSeconds(1)).build(),
                JwtClaimsSet.builder().issuer("wrong").subject("10").claim("role", 2)
                        .issuedAt(now).expiresAt(now.plusSeconds(900)).build(),
                JwtClaimsSet.builder().issuer("cram-school").subject("10").claim("role", 2).build())) {
            String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
            mvc.perform(get("/test/items").secure(true).header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test void getPermissionDoesNotGrantPost() throws Exception {
        String token = tokens.issue(10, 2).getAccessToken();
        assertTrace(mvc.perform(get("/test/items").secure(true).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value("ok")).andReturn());
        assertTrace(mvc.perform(post("/test/items").secure(true).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value("權限不足")).andReturn());
    }

    @Test void changedRoleDeletedAccountAndUnconfiguredApiAreDenied() throws Exception {
        String token = tokens.issue(10, 2).getAccessToken();
        mvc.perform(get("/unconfigured").secure(true).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        when(accounts.findCurrentRole(10)).thenReturn(3);
        mvc.perform(get("/test/items").secure(true).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        when(accounts.findCurrentRole(10)).thenReturn(null);
        mvc.perform(get("/test/items").secure(true).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test void untrustedAlgorithmAndMalformedTokensAreRejected() throws Exception {
        for (String token : Arrays.asList("invalid", "eyJhbGciOiJub25lIn0.eyJzdWIiOiIxMCIsInJvbGUiOjJ9.",
                "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMCJ9.invalid")) {
            mvc.perform(get("/test/items").secure(true).header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test void validationAndSystemExceptionResponsesHaveTraceId() throws Exception {
        assertTrace(mvc.perform(post("/auth/login").secure(true).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.code").value(CommonCode.PARAM_INVALID.getCode())).andReturn());
        Api api = new Api(); api.setPath("/test/failure"); api.setMethod(1);
        when(permissions.findApis(2, 1)).thenReturn(Collections.singletonList(api));
        assertTrace(mvc.perform(get("/test/failure").secure(true)
                .header("Authorization", "Bearer " + tokens.issue(10, 2).getAccessToken()))
                .andExpect(jsonPath("$.code").value(CommonCode.FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("Failed")).andReturn());
    }

    @Test void insecureHttpIsRedirectedToHttpsAndSecretsMustBeStrong() throws Exception {
        mvc.perform(post("/auth/login")).andExpect(status().is3xxRedirection());
        AuthProperties properties = new AuthProperties();
        assertThrows(IllegalStateException.class, () -> new SecurityConfig().jwtSigningKey(properties));
        properties.setJwtSecret("c2hvcnQ=");
        assertThrows(IllegalStateException.class, () -> new SecurityConfig().jwtSigningKey(properties));
    }

    private void assertTrace(MvcResult result) throws Exception {
        String id = result.getResponse().getHeader("X-Trace-Id");
        assertNotNull(id); assertTrue(id.matches("[a-f0-9]{32}"));
        assertEquals(id, json.readTree(result.getResponse().getContentAsString()).path("traceId").asText());
        assertNull(MDC.get("traceId"));
    }

    @Test void publicKeyIsAvailableBeforeLoginAndNeverContainsPrivateKey() throws Exception {
        MvcResult result = mvc.perform(post("/auth/public-key").secure(true).contentType(MediaType.APPLICATION_JSON)
                .content("{\"account\":\"tester\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.publicKey").value(material.getPublicKey()))
                .andExpect(jsonPath("$.data.privateKey").doesNotExist())
                .andExpect(jsonPath("$.data.algorithm").value("RSA-OAEP-256")).andReturn();
        assertTrace(result);
        mvc.perform(get("/auth/public-key").secure(true)).andExpect(status().isUnauthorized());
    }

    @Test void plaintextWrongKeyAndCorruptCiphertextCannotLogin() throws Exception {
        String otherKey = keys.generate().getPublicKey();
        for (String password : Arrays.asList("correct-password", "not-base64!",
                RsaTestSupport.encrypt(otherKey, "correct-password"))) {
            Map<String, String> body = new HashMap<>(); body.put("account", "tester"); body.put("password", password);
            assertTrace(mvc.perform(post("/auth/login").secure(true).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(body))).andExpect(status().isUnauthorized()).andReturn());
        }
    }

    private String loginBody(String account, String password) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("account", account);
        body.put("password", RsaTestSupport.encrypt(material.getPublicKey(), password));
        return json.writeValueAsString(body);
    }
}
