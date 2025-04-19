package com.adverge.sdk.utils;

import java.util.List;

/**
 * 安全配置类
 */
public class SecurityConfig {
    private final boolean enableEncryption;
    private final String encryptionKey;
    private final boolean enableSignature;
    private final String signatureKey;
    private final List<String> allowedOrigins;
    private final boolean enableIpBlacklist;
    private final long tokenExpiration;
    
    private SecurityConfig(Builder builder) {
        this.enableEncryption = builder.enableEncryption;
        this.encryptionKey = builder.encryptionKey;
        this.enableSignature = builder.enableSignature;
        this.signatureKey = builder.signatureKey;
        this.allowedOrigins = builder.allowedOrigins;
        this.enableIpBlacklist = builder.enableIpBlacklist;
        this.tokenExpiration = builder.tokenExpiration;
    }
    
    /**
     * 是否启用加密
     */
    public boolean isEnableEncryption() {
        return enableEncryption;
    }
    
    /**
     * 获取加密密钥
     */
    public String getEncryptionKey() {
        return encryptionKey;
    }
    
    /**
     * 是否启用签名
     */
    public boolean isEnableSignature() {
        return enableSignature;
    }
    
    /**
     * 获取签名密钥
     */
    public String getSignatureKey() {
        return signatureKey;
    }
    
    /**
     * 获取允许的源列表
     */
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
    
    /**
     * 是否启用IP黑名单
     */
    public boolean isEnableIpBlacklist() {
        return enableIpBlacklist;
    }
    
    /**
     * 获取令牌过期时间（毫秒）
     */
    public long getTokenExpiration() {
        return tokenExpiration;
    }
    
    /**
     * 配置构建器
     */
    public static class Builder {
        private boolean enableEncryption = true;
        private String encryptionKey = "default_encryption_key";
        private boolean enableSignature = true;
        private String signatureKey = "default_signature_key";
        private List<String> allowedOrigins = java.util.Arrays.asList("*");
        private boolean enableIpBlacklist = true;
        private long tokenExpiration = 24 * 60 * 60 * 1000; // 24小时
        
        /**
         * 设置是否启用加密
         */
        public Builder setEnableEncryption(boolean enableEncryption) {
            this.enableEncryption = enableEncryption;
            return this;
        }
        
        /**
         * 设置加密密钥
         */
        public Builder setEncryptionKey(String encryptionKey) {
            this.encryptionKey = encryptionKey;
            return this;
        }
        
        /**
         * 设置是否启用签名
         */
        public Builder setEnableSignature(boolean enableSignature) {
            this.enableSignature = enableSignature;
            return this;
        }
        
        /**
         * 设置签名密钥
         */
        public Builder setSignatureKey(String signatureKey) {
            this.signatureKey = signatureKey;
            return this;
        }
        
        /**
         * 设置允许的源列表
         */
        public Builder setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
            return this;
        }
        
        /**
         * 设置是否启用IP黑名单
         */
        public Builder setEnableIpBlacklist(boolean enableIpBlacklist) {
            this.enableIpBlacklist = enableIpBlacklist;
            return this;
        }
        
        /**
         * 设置令牌过期时间
         */
        public Builder setTokenExpiration(long tokenExpiration) {
            this.tokenExpiration = tokenExpiration;
            return this;
        }
        
        /**
         * 构建配置对象
         */
        public SecurityConfig build() {
            return new SecurityConfig(this);
        }
    }
} 