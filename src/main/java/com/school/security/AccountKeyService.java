package com.school.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AccountKeyService {
    private final AuthProperties properties;
    private final SecureRandom random = new SecureRandom();
    private SecretKey wrappingKey;

    @PostConstruct
    private void initialize() {
        try {
            byte[] bytes = Base64.getDecoder().decode(properties.getKeyEncryptionSecret());
            if (bytes.length != 32) throw new IllegalArgumentException();
            if (properties.getKeyEncryptionSecret().equals(properties.getJwtSecret())) throw new IllegalArgumentException();
            wrappingKey = new SecretKeySpec(bytes, "AES");
        } catch (RuntimeException exception) {
            throw new IllegalStateException("請設定 ACCOUNT_KEY_SECRET：Base64 編碼的獨立 32 bytes 隨機主金鑰");
        }
    }

    public KeyMaterial generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, random);
            KeyPair pair = generator.generateKeyPair();
            String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
            byte[] iv = new byte[12]; random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, wrappingKey, new GCMParameterSpec(128, iv));
            cipher.updateAAD(publicKey.getBytes(StandardCharsets.US_ASCII));
            byte[] sealed = cipher.doFinal(pair.getPrivate().getEncoded());
            String privateKey = "v1:" + Base64.getEncoder().encodeToString(
                    ByteBuffer.allocate(iv.length + sealed.length).put(iv).put(sealed).array());
            return new KeyMaterial(publicKey, privateKey);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("無法產生帳號金鑰", exception);
        }
    }

    public String decryptPassword(String encryptedPassword, String publicKey, String encryptedPrivateKey)
            throws GeneralSecurityException {
        byte[] privateBytes = null;
        byte[] passwordBytes = null;
        try {
            byte[] ciphertext = Base64.getDecoder().decode(encryptedPassword);
            if (ciphertext.length != 256 || encryptedPrivateKey == null || !encryptedPrivateKey.startsWith("v1:")) {
                throw new GeneralSecurityException("無效的加密資料");
            }
            byte[] envelope = Base64.getDecoder().decode(encryptedPrivateKey.substring(3));
            if (envelope.length < 29) throw new GeneralSecurityException("無效的加密資料");
            Cipher wrapper = Cipher.getInstance("AES/GCM/NoPadding");
            wrapper.init(Cipher.DECRYPT_MODE, wrappingKey, new GCMParameterSpec(128, Arrays.copyOf(envelope, 12)));
            wrapper.updateAAD(publicKey.getBytes(StandardCharsets.US_ASCII));
            privateBytes = wrapper.doFinal(envelope, 12, envelope.length - 12);
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
            Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            rsa.init(Cipher.DECRYPT_MODE, privateKey,
                    new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
            passwordBytes = rsa.doFinal(ciphertext);
            // BCrypt 不得靜默截斷超過 72 bytes 的輸入。
            if (passwordBytes.length == 0 || passwordBytes.length > 72) throw new GeneralSecurityException("密碼長度不正確");
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(passwordBytes)).toString();
        } catch (IllegalArgumentException | NullPointerException | java.nio.charset.CharacterCodingException exception) {
            throw new GeneralSecurityException("無效的加密資料");
        } finally {
            if (privateBytes != null) Arrays.fill(privateBytes, (byte) 0);
            if (passwordBytes != null) Arrays.fill(passwordBytes, (byte) 0);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class KeyMaterial {
        private final String publicKey;
        private final String encryptedPrivateKey;
    }
}
