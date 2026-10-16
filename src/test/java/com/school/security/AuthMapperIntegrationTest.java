package com.school.security;

import com.school.service.AccountService;
import com.school.service.ApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** 明確啟用後才連線本機 DB；所有 SQL 均唯讀。 */
@SpringBootTest(properties = {"security.auth.jwt-secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "security.auth.key-encryption-secret=YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODk="})
@EnabledIfEnvironmentVariable(named = "AUTH_DB_TEST", matches = "true")
class AuthMapperIntegrationTest {
    @Autowired AccountService accounts;
    @Autowired ApiService permissions;
    @Autowired JdbcTemplate jdbc;

    @Test void jdbcAndActualMapperQueriesWorkWithoutChangingData() {
        assertEquals("cram_school", jdbc.queryForObject("SELECT DATABASE()", String.class));
        assertTrue(accounts.findLoginAccounts(UUID.randomUUID().toString()).isEmpty());
        assertNull(accounts.findCurrentRole(-1));
        assertTrue(permissions.findApis(-1, 1).isEmpty());
        for (String table : new String[]{"account", "role", "role_menu", "menu", "menu_api", "api"}) {
            assertNotNull(jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Long.class));
        }
    }
}
