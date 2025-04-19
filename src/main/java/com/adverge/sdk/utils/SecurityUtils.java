package com.adverge.sdk.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 安全工具类
 */
public class SecurityUtils {
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String AES_KEY_ALGORITHM = "AES";
    private static final String MD5_ALGORITHM = "MD5";
    private static final String IV = "1234567890123456";

    /**
     * AES 加密
     */
    public String encrypt(String data, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_KEY_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            Logger.e("SecurityUtils", "Encryption failed", e);
            return null;
        }
    }

    /**
     * AES 解密
     */
    public String decrypt(String encryptedData, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_KEY_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Logger.e("SecurityUtils", "Decryption failed", e);
            return null;
        }
    }

    /**
     * MD5 加密
     */
    public String md5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            byte[] messageDigest = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Logger.e("SecurityUtils", "MD5 encryption failed", e);
            return null;
        }
    }

    /**
     * 生成请求签名
     */
    public String generateSignature(String data, String key) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String raw = data + timestamp + key;
        return md5(raw);
    }

    /**
     * 验证请求签名
     */
    public boolean verifySignature(String data, String signature, String key, long timestamp) {
        String raw = data + timestamp + key;
        String expectedSignature = md5(raw);
        return expectedSignature != null && expectedSignature.equals(signature);
    }
} 