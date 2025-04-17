import { logger } from './logger';

class CacheManager {
    constructor(config) {
        this.config = config;
        this.cache = new Map();
        this.maxSize = config.cache.maxSize || 100;
        this.ttl = config.cache.ttl || 300; // 默认5分钟
    }

    async get(key) {
        const item = this.cache.get(key);
        if (!item) return null;

        // 检查是否过期
        if (Date.now() > item.expires) {
            this.cache.delete(key);
            return null;
        }

        return item.value;
    }

    async set(key, value, ttl = this.ttl) {
        // 如果缓存已满，删除最旧的项
        if (this.cache.size >= this.maxSize) {
            const oldestKey = this.cache.keys().next().value;
            this.cache.delete(oldestKey);
        }

        this.cache.set(key, {
            value,
            expires: Date.now() + ttl * 1000
        });
    }

    async delete(key) {
        this.cache.delete(key);
    }

    async clear() {
        this.cache.clear();
    }

    // 定期清理过期缓存
    startCleanup() {
        setInterval(() => {
            const now = Date.now();
            for (const [key, item] of this.cache.entries()) {
                if (now > item.expires) {
                    this.cache.delete(key);
                }
            }
        }, 60000); // 每分钟清理一次
    }
}

export default CacheManager; 