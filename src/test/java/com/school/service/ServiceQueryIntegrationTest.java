package com.school.service;

import com.school.dto.LoginDTO;
import com.school.entity.Api;
import com.school.dto.PublicKeyDTO;
import com.school.dto.RegisterDTO;
import com.school.security.AccountKeyService;
import com.school.support.RsaTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** 僅在獨立 H2 資料庫建立測試資料，不接觸本機 MySQL。 */
@SpringBootTest(properties = {
        "security.auth.jwt-secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "security.auth.key-encryption-secret=YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODk=",
        "spring.datasource.url=jdbc:h2:mem:service_queries;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password="})
@Sql("/service-query-fixture.sql")
@AutoConfigureMockMvc
class ServiceQueryIntegrationTest {
    @Autowired AccountService accounts;
    @Autowired ApiService apis;
    @Autowired AuthService authentication;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired AccountKeyService keys;
    @Autowired ObjectMapper json;
    @Autowired MockMvc mvc;

    @Test void loginLookupPreservesFieldsAndLimitsDuplicateAccountsToTwo() {
        assertEquals(1, accounts.findLoginAccounts("tester").size());
        assertEquals("fixture", accounts.findLoginAccounts("tester").get(0).getPassword());
        assertEquals(2, accounts.findLoginAccounts("duplicate").size());
        assertTrue(accounts.findLoginAccounts("missing").isEmpty());
        assertTrue(accounts.findLoginAccounts(null).isEmpty());
    }

    @Test void currentRoleRequiresBothAccountAndRoleToExist() {
        assertEquals(Integer.valueOf(1), accounts.findCurrentRole(1));
        assertNull(accounts.findCurrentRole(2));
        assertNull(accounts.findCurrentRole(-1));
        assertNull(accounts.findCurrentRole(null));
        jdbc.update("DELETE FROM role WHERE id = 1");
        assertNull(accounts.findCurrentRole(1));
    }

    @Test void permissionsAreDeduplicatedAndFilteredByMethodAndValidRelations() {
        List<Api> getApis = apis.findApis(1, 1);
        assertEquals(1, getApis.size());
        assertEquals(Integer.valueOf(1), getApis.get(0).getId());
        assertEquals("/same", getApis.get(0).getPath());
        assertEquals(Integer.valueOf(2), apis.findApis(1, 2).get(0).getId());
        assertTrue(apis.findApis(1, 4).isEmpty());
    }

    @Test void emptyRoleMenuAndMenuApiRelationsCannotExposeAllApis() {
        assertTrue(apis.findApis(2, 1).isEmpty());
        assertTrue(apis.findApis(3, 1).isEmpty());
        assertTrue(apis.findApis(-1, 1).isEmpty());
        assertTrue(apis.findApis(null, 1).isEmpty());
        assertTrue(apis.findApis(1, null).isEmpty());
        jdbc.update("DELETE FROM menu_api WHERE menu_id IN (1, 2)");
        assertTrue(apis.findApis(1, 1).isEmpty());
    }

    @Test void removedMenusOrRoleImmediatelyRemovePermission() {
        jdbc.update("DELETE FROM menu WHERE id IN (1, 2)");
        assertTrue(apis.findApis(1, 1).isEmpty());
        jdbc.update("DELETE FROM role WHERE id = 1");
        assertTrue(apis.findApis(1, 1).isEmpty());
    }

    @Test void loginWorksThroughRealServiceQueriesAndRejectsDuplicateAccounts() throws Exception {
        AccountKeyService.KeyMaterial key = keys.generate();
        jdbc.update("UPDATE account SET password = ?, public_key = ?, private_key = ? WHERE id = 1",
                encoder.encode("test-password"), key.getPublicKey(), key.getEncryptedPrivateKey());
        LoginDTO request = new LoginDTO();
        request.setAccount("tester"); request.setPassword(RsaTestSupport.encrypt(key.getPublicKey(), "test-password"));
        assertEquals(Integer.valueOf(1), authentication.login(request).getRole());
        request.setAccount("duplicate");
        assertThrows(BadCredentialsException.class, () -> authentication.login(request));
    }

    @Test void registrationPersistsEncryptedKeysAndBcryptAndAllowsRsaLogin() throws Exception {
        RegisterDTO registration = new RegisterDTO();
        registration.setAccount("new-parent"); registration.setName("新家長"); registration.setPassword("abc12345");
        authentication.register(registration);
        PublicKeyDTO query = new PublicKeyDTO(); query.setAccount("new-parent");
        String publicKey = authentication.publicKey(query).getPublicKey();
        String savedPrivateKey = jdbc.queryForObject("SELECT private_key FROM account WHERE account = 'new-parent'", String.class);
        assertTrue(savedPrivateKey.startsWith("v1:"));
        String hash = jdbc.queryForObject("SELECT password FROM account WHERE account = 'new-parent'", String.class);
        assertTrue(encoder.matches("abc12345", hash));
        LoginDTO login = new LoginDTO(); login.setAccount("new-parent");
        login.setPassword(RsaTestSupport.encrypt(publicKey, "abc12345"));
        assertEquals(Integer.valueOf(4), authentication.login(login).getRole());
        String accountJson = json.writeValueAsString(accounts.findLoginAccounts("new-parent").get(0));
        assertFalse(accountJson.contains("privateKey")); assertFalse(accountJson.contains("password"));
        assertFalse(accountJson.contains("token"));
        assertThrows(IllegalArgumentException.class, () -> authentication.register(registration));
    }

    @Test void registrationPublicKeyAndLoginApisWorkTogetherWithoutGrantingClientSelectedRole() throws Exception {
        mvc.perform(post("/auth/register").secure(true).contentType(MediaType.APPLICATION_JSON)
                .content("{\"account\":\"http-parent\",\"name\":\"家長\",\"password\":\"abc12345\",\"role\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
        assertEquals(Integer.valueOf(4), jdbc.queryForObject("SELECT role FROM account WHERE account='http-parent'", Integer.class));
        MvcResult keyResult = mvc.perform(post("/auth/public-key").secure(true).contentType(MediaType.APPLICATION_JSON)
                .content("{\"account\":\"http-parent\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.privateKey").doesNotExist()).andReturn();
        String publicKey = json.readTree(keyResult.getResponse().getContentAsString()).path("data").path("publicKey").asText();
        Map<String, String> body = new HashMap<>(); body.put("account", "http-parent");
        body.put("password", RsaTestSupport.encrypt(publicKey, "abc12345"));
        mvc.perform(post("/auth/login").secure(true).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value(4)).andExpect(jsonPath("$.data.expiresIn").value(900));
    }
}
