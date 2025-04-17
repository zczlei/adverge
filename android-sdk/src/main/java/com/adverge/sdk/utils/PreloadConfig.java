package com.adverge.sdk.utils;

/**
 * 预加载配置类
 */
public class PreloadConfig {
    private final boolean enablePreload;
    private final int preloadCount;
    private final long preloadTimeout;
    private final boolean enableAutoPreload;
    private final long autoPreloadInterval;
    
    private PreloadConfig(Builder builder) {
        this.enablePreload = builder.enablePreload;
        this.preloadCount = builder.preloadCount;
        this.preloadTimeout = builder.preloadTimeout;
        this.enableAutoPreload = builder.enableAutoPreload;
        this.autoPreloadInterval = builder.autoPreloadInterval;
    }
    
    /**
     * 是否启用预加载
     */
    public boolean isEnablePreload() {
        return enablePreload;
    }
    
    /**
     * 获取预加载数量
     */
    public int getPreloadCount() {
        return preloadCount;
    }
    
    /**
     * 获取预加载超时时间（毫秒）
     */
    public long getPreloadTimeout() {
        return preloadTimeout;
    }
    
    /**
     * 是否启用自动预加载
     */
    public boolean isEnableAutoPreload() {
        return enableAutoPreload;
    }
    
    /**
     * 获取自动预加载间隔（毫秒）
     */
    public long getAutoPreloadInterval() {
        return autoPreloadInterval;
    }
    
    /**
     * 配置构建器
     */
    public static class Builder {
        private boolean enablePreload = true;
        private int preloadCount = 3;
        private long preloadTimeout = 5000;
        private boolean enableAutoPreload = true;
        private long autoPreloadInterval = 300000; // 5分钟
        
        /**
         * 设置是否启用预加载
         */
        public Builder setEnablePreload(boolean enablePreload) {
            this.enablePreload = enablePreload;
            return this;
        }
        
        /**
         * 设置预加载数量
         */
        public Builder setPreloadCount(int preloadCount) {
            this.preloadCount = preloadCount;
            return this;
        }
        
        /**
         * 设置预加载超时时间
         */
        public Builder setPreloadTimeout(long preloadTimeout) {
            this.preloadTimeout = preloadTimeout;
            return this;
        }
        
        /**
         * 设置是否启用自动预加载
         */
        public Builder setEnableAutoPreload(boolean enableAutoPreload) {
            this.enableAutoPreload = enableAutoPreload;
            return this;
        }
        
        /**
         * 设置自动预加载间隔
         */
        public Builder setAutoPreloadInterval(long autoPreloadInterval) {
            this.autoPreloadInterval = autoPreloadInterval;
            return this;
        }
        
        /**
         * 构建配置对象
         */
        public PreloadConfig build() {
            return new PreloadConfig(this);
        }
    }
} 