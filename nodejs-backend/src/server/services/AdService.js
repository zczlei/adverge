const cacheService = require('./CacheService');
const rateLimitService = require('./RateLimitService');
const monitoringService = require('./MonitoringService');
const securityService = require('./SecurityService');
const biddingService = require('./BiddingService');
const logger = require('../utils/logger');

class AdService {
    constructor() {
        // 创建广告请求限流器
        this.requestLimiter = rateLimitService.createLimiter({
            name: 'ad-requests',
            windowMs: 60 * 1000, // 1分钟
            max: 100, // 限制100次请求
            message: '广告请求过于频繁，请稍后再试'
        });

        // 创建竞价限流器
        this.bidLimiter = rateLimitService.createLimiter({
            name: 'ad-bids',
            windowMs: 60 * 1000, // 1分钟
            max: 50, // 限制50次竞价
            message: '竞价请求过于频繁，请稍后再试'
        });

        // 启动监控报告
        monitoringService.startReporting();
    }

    async getAd(adUnitId, options, req) {
        logger.info(`收到广告请求: ${adUnitId}`);
        
        // 验证请求安全性
        this.validateRequest(req);

        monitoringService.recordRequest();
        const startTime = Date.now();

        try {
            // 检查请求限流
            if (!await this.checkRateLimit('ad-requests', adUnitId)) {
                throw new Error('请求过于频繁');
            }

            // 尝试从缓存获取广告
            const cacheKey = this.generateCacheKey(adUnitId, options);
            const cachedAd = await cacheService.get(cacheKey);
            
            if (cachedAd) {
                monitoringService.recordCacheHit();
                logger.info(`广告缓存命中: ${adUnitId}`);
                return this.encryptAdData(cachedAd);
            }

            monitoringService.recordCacheMiss();

            // 1. 向支持bidding的平台发起询价
            logger.info(`开始竞价流程: ${adUnitId}`);
            const winningBid = await biddingService.processBid(adUnitId, {
                ...options,
                deviceInfo: this.getDeviceInfo(req),
                userInfo: this.getUserInfo(req)
            });

            if (!winningBid) {
                throw new Error('未找到合适的广告');
            }

            // 2. 构建广告响应
            const ad = {
                adUnitId,
                platform: winningBid.platform,
                adType: options.adType,
                creative: winningBid.creative,
                price: winningBid.price,
                currency: winningBid.currency,
                tracking: {
                    impression: winningBid.impressionUrl,
                    click: winningBid.clickUrl
                }
            };

            // 3. 缓存广告
            await cacheService.set(cacheKey, ad, 300); // 缓存5分钟

            // 记录性能指标
            const loadTime = Date.now() - startTime;
            monitoringService.recordPerformance('ad_load_time', loadTime);
            monitoringService.trackPlatformPerformance(ad.platform, 'load_time', loadTime);

            return this.encryptAdData(ad);
        } catch (error) {
            monitoringService.recordError(error);
            logger.error(`获取广告失败: ${error.message}`);
            throw error;
        }
    }

    async trackImpression(adId, platform, req) {
        logger.info(`记录广告展示: ${adId}`);
        
        // 验证请求安全性
        this.validateRequest(req);

        try {
            monitoringService.recordAdImpression(adId, platform);
            await biddingService.recordImpression(adId, platform);
        } catch (error) {
            logger.error(`记录广告展示失败: ${error.message}`);
        }
    }

    async trackClick(adId, platform, revenue, req) {
        logger.info(`记录广告点击: ${adId}`);
        
        // 验证请求安全性
        this.validateRequest(req);

        try {
            monitoringService.recordAdClick(adId, platform);
            await biddingService.recordClick(adId, platform);
            if (revenue) {
                monitoringService.recordRevenue(revenue, platform);
            }
        } catch (error) {
            logger.error(`记录广告点击失败: ${error.message}`);
        }
    }

    // 验证请求安全性
    validateRequest(req) {
        // 验证请求来源
        securityService.validateRequestOrigin(req);

        // 验证请求签名
        securityService.validateRequestSignature(req);

        // 检查IP是否在黑名单中
        const clientIp = req.ip;
        if (securityService.isIpBlacklisted(clientIp)) {
            throw new Error('IP已被列入黑名单');
        }
    }

    // 加密广告数据
    encryptAdData(adData) {
        return securityService.encryptData(adData);
    }

    async checkRateLimit(type, key) {
        try {
            const stats = await rateLimitService.getRateLimitStats(`${type}:${key}`);
            if (stats && stats.remaining <= 0) {
                return false;
            }
            return true;
        } catch (error) {
            logger.error(`检查限流失败: ${error.message}`);
            return true; // 限流检查失败时允许请求
        }
    }

    generateCacheKey(adUnitId, options) {
        const { width, height, adType } = options;
        return `ad:${adUnitId}:${width}:${height}:${adType}`;
    }

    getDeviceInfo(req) {
        return {
            userAgent: req.headers['user-agent'],
            ip: req.ip,
            language: req.headers['accept-language'],
            screenSize: req.query.screenSize || 'unknown',
            deviceType: this.detectDeviceType(req.headers['user-agent'])
        };
    }

    getUserInfo(req) {
        return {
            userId: req.query.userId || 'unknown',
            country: req.query.country || 'unknown',
            language: req.query.language || 'unknown'
        };
    }

    detectDeviceType(userAgent) {
        if (!userAgent) return 'unknown';
        if (userAgent.includes('Android')) return 'android';
        if (userAgent.includes('iPhone') || userAgent.includes('iPad')) return 'ios';
        return 'web';
    }
}

module.exports = new AdService(); 