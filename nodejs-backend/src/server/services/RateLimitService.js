const rateLimit = require('express-rate-limit');
const Redis = require('ioredis');
const logger = require('../utils/logger');

class RateLimitService {
    constructor() {
        this.redis = new Redis({
            host: process.env.REDIS_HOST || 'localhost',
            port: process.env.REDIS_PORT || 6379,
            password: process.env.REDIS_PASSWORD
        });

        this.limits = new Map();
    }

    createLimiter(options) {
        const {
            windowMs = 15 * 60 * 1000, // 15分钟
            max = 100, // 限制次数
            keyGenerator = (req) => req.ip, // 默认使用IP作为key
            message = '请求过于频繁，请稍后再试',
            skip = () => false, // 默认不跳过任何请求
            handler = (req, res) => {
                res.status(429).json({ error: message });
            }
        } = options;

        const limiter = rateLimit({
            windowMs,
            max,
            keyGenerator,
            skip,
            handler,
            store: new RedisStore({
                client: this.redis,
                prefix: 'rate-limit:'
            })
        });

        this.limits.set(options.name, limiter);
        return limiter;
    }

    getLimiter(name) {
        return this.limits.get(name);
    }

    async getRateLimitStats(key) {
        try {
            const stats = await this.redis.hgetall(`rate-limit:${key}`);
            return {
                current: parseInt(stats.current || 0),
                resetTime: parseInt(stats.resetTime || 0),
                remaining: parseInt(stats.remaining || 0)
            };
        } catch (error) {
            logger.error(`获取限流统计失败: ${error.message}`);
            return null;
        }
    }

    async resetRateLimit(key) {
        try {
            await this.redis.del(`rate-limit:${key}`);
            logger.info(`重置限流: ${key}`);
            return true;
        } catch (error) {
            logger.error(`重置限流失败: ${error.message}`);
            return false;
        }
    }
}

class RedisStore {
    constructor(options) {
        this.client = options.client;
        this.prefix = options.prefix || 'rate-limit:';
    }

    async increment(key, cb) {
        const current = await this.client.incr(this.prefix + key);
        if (current === 1) {
            await this.client.expire(this.prefix + key, 60);
        }
        cb(null, current);
    }

    async decrement(key) {
        await this.client.decr(this.prefix + key);
    }

    async resetKey(key) {
        await this.client.del(this.prefix + key);
    }
}

module.exports = new RateLimitService(); 