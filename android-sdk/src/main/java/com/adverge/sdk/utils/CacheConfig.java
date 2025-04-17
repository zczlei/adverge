package com.adverge.sdk.utils;

/**
 * 缓存配置类
 */
public class CacheConfig {
    private final long maxSize;
    private final long maxAge;
    private final boolean enableMemoryCache;
    private final boolean enableDiskCache;
    private final String cacheDir;
    
    private CacheConfig(Builder builder) {
        this.maxSize = builder.maxSize;
        this.maxAge = builder.maxAge;
        this.enableMemoryCache = builder.enableMemoryCache;
        this.enableDiskCache = builder.enableDiskCache;
        this.cacheDir = builder.cacheDir;
    }
    
    /**
     * 获取最大缓存大小（字节）
     */
    public long getMaxSize() {
        return maxSize;
    }
    
    /**
     * 获取缓存最大有效期（毫秒）
     */
    public long getMaxAge() {
        return maxAge;
    }
    
    /**
     * 是否启用内存缓存
     */
    public boolean isEnableMemoryCache() {
        return enableMemoryCache;
    }
    
    /**
     * 是否启用磁盘缓存
     */
    public boolean isEnableDiskCache() {
        return enableDiskCache;
    }
    
    /**
     * 获取缓存目录
     */
    public String getCacheDir() {
        return cacheDir;
    }
    
    /**
     * 配置构建器
     */
    public static class Builder {
        private long maxSize = 50 * 1024 * 1024; // 50MB
        private long maxAge = 7 * 24 * 60 * 60 * 1000; // 7天
        private boolean enableMemoryCache = true;
        private boolean enableDiskCache = true;
        private String cacheDir = "ad_cache";
        
        /**
         * 设置最大缓存大小
         */
        public Builder setMaxSize(long maxSize) {
            this.maxSize = maxSize;
            return this;
        }
        
        /**
         * 设置缓存最大有效期
         */
        public Builder setMaxAge(long maxAge) {
            this.maxAge = maxAge;
            return this;
        }
        
        /**
         * 设置是否启用内存缓存
         */
        public Builder setEnableMemoryCache(boolean enableMemoryCache) {
            this.enableMemoryCache = enableMemoryCache;
            return this;
        }
        
        /**
         * 设置是否启用磁盘缓存
         */
        public Builder setEnableDiskCache(boolean enableDiskCache) {
            this.enableDiskCache = enableDiskCache;
            return this;
        }
        
        /**
         * 设置缓存目录
         */
        public Builder setCacheDir(String cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }
        
        /**
         * 构建配置对象
         */
        public CacheConfig build() {
            return new CacheConfig(this);
        }
    }
} 