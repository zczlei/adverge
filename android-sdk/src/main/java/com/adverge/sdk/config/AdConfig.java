package com.adverge.sdk.config;

import com.adverge.sdk.utils.CacheConfig;
import com.adverge.sdk.utils.PreloadConfig;
import com.adverge.sdk.utils.SecurityConfig;

/**
 * 广告配置类
 */
public class AdConfig {
    private String appId;
    private String appKey;
    private CacheConfig cacheConfig;
    private PreloadConfig preloadConfig;
    private SecurityConfig securityConfig;
    private boolean debugMode;
    private boolean testMode;
    private int maxConcurrentRequests;
    private int requestTimeout;
    
    public AdConfig() {
        // 默认配置
        this.appId = "com.adverge.sdk.demo";
        this.appKey = "AdVerge-SDK-Demo-Key";
        this.cacheConfig = new CacheConfig.Builder().build();
        this.preloadConfig = new PreloadConfig.Builder().build();
        this.securityConfig = new SecurityConfig.Builder().build();
        this.debugMode = false;
        this.testMode = false;
        this.maxConcurrentRequests = 5;
        this.requestTimeout = 10000;
    }
    
    private AdConfig(Builder builder) {
        this.appId = builder.appId;
        this.appKey = builder.appKey;
        this.cacheConfig = builder.cacheConfig;
        this.preloadConfig = builder.preloadConfig;
        this.securityConfig = builder.securityConfig;
        this.debugMode = builder.debugMode;
        this.testMode = builder.testMode;
        this.maxConcurrentRequests = builder.maxConcurrentRequests;
        this.requestTimeout = builder.requestTimeout;
    }
    
    /**
     * 获取应用ID
     */
    public String getAppId() {
        return appId;
    }
    
    /**
     * 设置应用ID
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }
    
    /**
     * 获取应用密钥
     */
    public String getAppKey() {
        return appKey;
    }
    
    /**
     * 设置应用密钥
     */
    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
    
    /**
     * 获取缓存配置
     */
    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }
    
    /**
     * 获取预加载配置
     */
    public PreloadConfig getPreloadConfig() {
        return preloadConfig;
    }
    
    /**
     * 获取安全配置
     */
    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }
    
    /**
     * 是否调试模式
     */
    public boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * 是否测试模式
     */
    public boolean isTestMode() {
        return testMode;
    }
    
    /**
     * 获取最大并发请求数
     */
    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }
    
    /**
     * 获取请求超时时间（毫秒）
     */
    public int getRequestTimeout() {
        return requestTimeout;
    }
    
    /**
     * 配置构建器
     */
    public static class Builder {
        private String appId = "com.adverge.sdk.demo";
        private String appKey = "AdVerge-SDK-Demo-Key";
        private CacheConfig cacheConfig = new CacheConfig.Builder().build();
        private PreloadConfig preloadConfig = new PreloadConfig.Builder().build();
        private SecurityConfig securityConfig = new SecurityConfig.Builder().build();
        private boolean debugMode = false;
        private boolean testMode = false;
        private int maxConcurrentRequests = 5;
        private int requestTimeout = 10000;
        
        /**
         * 设置应用ID
         */
        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }
        
        /**
         * 设置应用密钥
         */
        public Builder setAppKey(String appKey) {
            this.appKey = appKey;
            return this;
        }
        
        /**
         * 设置缓存配置
         */
        public Builder setCacheConfig(CacheConfig cacheConfig) {
            this.cacheConfig = cacheConfig;
            return this;
        }
        
        /**
         * 设置预加载配置
         */
        public Builder setPreloadConfig(PreloadConfig preloadConfig) {
            this.preloadConfig = preloadConfig;
            return this;
        }
        
        /**
         * 设置安全配置
         */
        public Builder setSecurityConfig(SecurityConfig securityConfig) {
            this.securityConfig = securityConfig;
            return this;
        }
        
        /**
         * 设置调试模式
         */
        public Builder setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }
        
        /**
         * 设置测试模式
         */
        public Builder setTestMode(boolean testMode) {
            this.testMode = testMode;
            return this;
        }
        
        /**
         * 设置最大并发请求数
         */
        public Builder setMaxConcurrentRequests(int maxConcurrentRequests) {
            this.maxConcurrentRequests = maxConcurrentRequests;
            return this;
        }
        
        /**
         * 设置请求超时时间
         */
        public Builder setRequestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }
        
        /**
         * 构建配置对象
         */
        public AdConfig build() {
            return new AdConfig(this);
        }
    }
} 