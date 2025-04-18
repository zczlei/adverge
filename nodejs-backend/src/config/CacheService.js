const NodeCache = require('node-cache');
const logger = require('../utils/logger');

class CacheService {
    constructor() {
        this.cache = new NodeCache({
            stdTTL: 300, // 默认缓存时间5分钟
            checkperiod: 60, // 检查过期时间间隔
            useClones: false // 禁用克隆，提高性能
        });
    }

    async get(key) {
        try {
            const value = this.cache.get(key);
            if (value) {
                logger.info(`缓存命中: ${key}`);
                return value;
            }
            logger.info(`缓存未命中: ${key}`);
            return null;
        } catch (error) {
            logger.error(`获取缓存失败: ${error.message}`);
            return null;
        }
    }

    async set(key, value, ttl = 300) {
        try {
            const success = this.cache.set(key, value, ttl);
            if (success) {
                logger.info(`缓存设置成功: ${key}`);
                return true;
            }
            logger.warn(`缓存设置失败: ${key}`);
            return false;
        } catch (error) {
            logger.error(`设置缓存失败: ${error.message}`);
            return false;
        }
    }

    async del(key) {
        try {
            this.cache.del(key);
            logger.info(`缓存删除成功: ${key}`);
            return true;
        } catch (error) {
            logger.error(`删除缓存失败: ${error.message}`);
            return false;
        }
    }

    async getStats() {
        try {
            const stats = this.cache.getStats();
            return {
                hits: stats.hits,
                misses: stats.misses,
                keys: stats.keys,
                ksize: stats.ksize,
                vsize: stats.vsize
            };
        } catch (error) {
            logger.error(`获取缓存统计失败: ${error.message}`);
            return null;
        }
    }

    async clear() {
        try {
            this.cache.flushAll();
            logger.info('缓存已清空');
            return true;
        } catch (error) {
            logger.error(`清空缓存失败: ${error.message}`);
            return false;
        }
    }
}

module.exports = new CacheService(); 