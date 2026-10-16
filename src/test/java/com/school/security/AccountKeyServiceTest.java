package com.school.security;

import com.school.support.RsaTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.util.ReflectionTestUtils;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountKeyServiceTest {
    private AccountKeyService service;
    private AccountKeyService.KeyMaterial first;
    private AccountKeyService.KeyMaterial second;

    @BeforeAll void initialize() {
        AuthProperties properties = new AuthProperties();
        properties.setKeyEncryptionSecret("YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODk=");
        service = new AccountKeyService(properties);
        ReflectionTestUtils.invokeMethod(service, "initialize");
        first = service.generate(); second = service.generate();
    }

    @Test void distinctAccountsHaveDifferentKeysAndOaepEncryptionIsRandomized() throws Exception {
        assertNotEquals(first.getPublicKey(), second.getPublicKey());
        String one = RsaTestSupport.encrypt(first.getPublicKey(), "繁體abc123");
        String two = RsaTestSupport.encrypt(first.getPublicKey(), "繁體abc123");
        assertNotEquals(one, two);
        assertEquals("繁體abc123", decrypt(one));
        assertEquals("繁體abc123", decrypt(two));
    }

    @Test void crossAccountPrivateKeysAndPublicKeyBindingAreRejected() throws Exception {
        String encrypted = RsaTestSupport.encrypt(first.getPublicKey(), "abc123");
        assertThrows(GeneralSecurityException.class, () -> service.decryptPassword(encrypted,
                second.getPublicKey(), second.getEncryptedPrivateKey()));
        assertThrows(GeneralSecurityException.class, () -> service.decryptPassword(encrypted,
                second.getPublicKey(), first.getEncryptedPrivateKey()));
    }

    @Test void modifiedPrivateKeyAndCiphertextAreRejected() throws Exception {
        String encrypted = RsaTestSupport.encrypt(first.getPublicKey(), "abc123");
        byte[] bytes = Base64.getDecoder().decode(first.getEncryptedPrivateKey().substring(3));
        bytes[bytes.length - 1] ^= 1;
        String alteredPrivateKey = "v1:" + Base64.getEncoder().encodeToString(bytes);
        assertThrows(GeneralSecurityException.class, () -> service.decryptPassword(encrypted,
                first.getPublicKey(), alteredPrivateKey));
        byte[] ciphertext = Base64.getDecoder().decode(encrypted); ciphertext[0] ^= 1;
        assertThrows(GeneralSecurityException.class,
                () -> decrypt(Base64.getEncoder().encodeToString(ciphertext)));
    }

    @Test void malformedAndOversizedPasswordsAreRejected() throws Exception {
        for (String value : Arrays.asList("abc123", "not-base64!", "", "YWJj")) {
            assertThrows(GeneralSecurityException.class, () -> decrypt(value));
        }
        char[] chars = new char[73]; Arrays.fill(chars, 'a');
        String tooLong = RsaTestSupport.encrypt(first.getPublicKey(), new String(chars));
        assertThrows(GeneralSecurityException.class, () -> decrypt(tooLong));
    }

    @Test void sha1MaskGenerationCannotBeUsedInsteadOfSha256() throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(
                new X509EncodedKeySpec(Base64.getDecoder().decode(first.getPublicKey()))),
                new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT));
        String wrong = Base64.getEncoder().encodeToString(cipher.doFinal("abc123".getBytes(StandardCharsets.UTF_8)));
        assertThrows(GeneralSecurityException.class, () -> decrypt(wrong));
    }

    @Test void wrappingKeyMustBeAnIndependentStrongSecret() {
        AuthProperties properties = new AuthProperties();
        AccountKeyService invalid = new AccountKeyService(properties);
        assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(invalid, "initialize"));
        properties.setKeyEncryptionSecret("c2hvcnQ=");
        assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(invalid, "initialize"));
        properties.setKeyEncryptionSecret("YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODk=");
        properties.setJwtSecret(properties.getKeyEncryptionSecret());
        assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(invalid, "initialize"));
    }

    private String decrypt(String value) throws GeneralSecurityException {
        return service.decryptPassword(value, first.getPublicKey(), first.getEncryptedPrivateKey());
    }
}
