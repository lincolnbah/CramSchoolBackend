package com.school.common.utils;

import com.school.common.common.api.CommonCode;
import com.school.common.common.exception.ApiException;
import com.school.common.dto.RSAKeyDTO;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

/**
 * RSA工具
 */
public final class RSAUtil {

    private static final int KEY_SIZE = 2048;

    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private static final OAEPParameterSpec OAEP = new OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    /**
     * 建立公私鑰
     */
    public static RSAKeyDTO generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(KEY_SIZE);
            KeyPair pair = generator.generateKeyPair();
            RSAKeyDTO rsaKeyDto = new RSAKeyDTO();
            rsaKeyDto.setPublicKey(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
            rsaKeyDto.setPrivateKey(Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
            return rsaKeyDto;
        } catch (Exception e) {
            throw new ApiException(CommonCode.CREATE_RSA_KEY_ERROR);
        }
    }

    /**
     * 加密
     *
     * @param plaintext       待加密文本
     * @param publicKeyBase64 公鑰
     * @return 加密資料
     */
    public static String encrypt(String plaintext, String publicKeyBase64)
            throws GeneralSecurityException {
        Objects.requireNonNull(plaintext, "Plaintext must not be null");
        PublicKey key = KeyFactory.getInstance("RSA").generatePublic(
                new X509EncodedKeySpec(decodeBase64(publicKeyBase64)));
        int keyBytes = validateKey((RSAKey) key);
        byte[] bytes = plaintext.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > keyBytes - 2 * 32 - 2) {
            throw new ApiException(CommonCode.PLAINTEXT_EXCEEDS_THE_LIMIT);
        }
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, OAEP);
        return Base64.getEncoder().encodeToString(cipher.doFinal(bytes));
    }

    /**
     * 解密
     *
     * @param ciphertextBase64 待解密文本
     * @param privateKeyBase64 私鑰
     * @return 解密文本
     */
    public static String decrypt(String ciphertextBase64, String privateKeyBase64) {
        PrivateKey key = null;
        try {
            key = KeyFactory.getInstance("RSA").generatePrivate(
                    new PKCS8EncodedKeySpec(decodeBase64(privateKeyBase64)));

            int keyBytes = validateKey((RSAKey) key);
            byte[] bytes = decodeBase64(ciphertextBase64);
            if (bytes.length != keyBytes) {
                throw new ApiException(CommonCode.CIPHERTEXT_DOES_NOT_MATCH);
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, OAEP);
            return new String(cipher.doFinal(bytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ApiException(CommonCode.DECRYPT_ERROR);
        }
    }

    private static byte[] decodeBase64(String value) {
        return Base64.getDecoder().decode(Objects.requireNonNull(value, "Base64 input must not be null"));
    }

    private static int validateKey(RSAKey key) {
        if (key.getModulus().bitLength() < KEY_SIZE) {
            throw new IllegalArgumentException("RSA key must be at least 2048 bits");
        }
        return (key.getModulus().bitLength() + 7) / 8;
    }

}
