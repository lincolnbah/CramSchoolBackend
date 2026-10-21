package com.school.common.utils;

import com.school.common.common.api.CommonCode;
import com.school.common.common.exception.ApiException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class SHAUtil {

    public static String sha256(String value) {
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            byte[] bytes = instance.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte byteValue : bytes) {
                result.append(String.format(Locale.ROOT, "%02x", byteValue & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new ApiException(CommonCode.ENCRYPTION_FAILED);
        }
    }


}
