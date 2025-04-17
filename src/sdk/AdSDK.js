import axios from 'axios';
import { SecurityService } from './services/SecurityService';
import CacheManager from './utils/CacheManager';
import RetryManager from './utils/RetryManager';
import { logger } from './utils/logger';

class AdSDK {
    constructor(config) {
        this.config = config;
        this.securityService = new SecurityService(config);
        this.cacheManager = new CacheManager(config);
        this.retryManager = new RetryManager(config);
        this.initialized = false;
    }

    async initialize() {
        try {
            // 验证配置
            this.validateConfig();

            // 初始化安全服务
            await this.securityService.initialize();

            // 设置axios默认配置
            axios.defaults.baseURL = this.config.apiUrl;
            axios.defaults.headers.common['Content-Type'] = 'application/json';
            axios.defaults.timeout = this.config.timeout || 5000;

            // 启动缓存清理
            this.cacheManager.startCleanup();

            this.initialized = true;
            logger.info('SDK初始化成功');
        } catch (error) {
            logger.error(`SDK初始化失败: ${error.message}`);
            throw error;
        }
    }

    async getAd(adUnitId, options = {}) {
        this.checkInitialization();

        try {
            // 生成缓存键
            const cacheKey = this.generateCacheKey('ad', adUnitId, options);
            
            // 尝试从缓存获取
            const cachedAd = await this.cacheManager.get(cacheKey);
            if (cachedAd) {
                logger.info(`广告缓存命中: ${adUnitId}`);
                return cachedAd;
            }

            // 生成请求签名
            const timestamp = Date.now();
            const signature = this.securityService.generateRequestSignature({
                adUnitId,
                options,
                timestamp
            });

            // 发送请求（带重试）
            const response = await this.retryManager.execute(async () => {
                return await axios.get(`/ad/${adUnitId}`, {
                    params: options,
                    headers: {
                        'X-Timestamp': timestamp,
                        'X-Signature': signature
                    }
                });
            });

            // 验证响应
            this.securityService.validateResponse(response);

            // 解密广告数据
            const decryptedAd = this.securityService.decryptData(response.data);
            
            // 缓存广告
            await this.cacheManager.set(cacheKey, decryptedAd);
            
            return decryptedAd;
        } catch (error) {
            logger.error(`获取广告失败: ${error.message}`);
            throw error;
        }
    }

    async bid(adUnitId, options = {}) {
        this.checkInitialization();

        try {
            // 生成缓存键
            const cacheKey = this.generateCacheKey('bid', adUnitId, options);
            
            // 尝试从缓存获取
            const cachedBid = await this.cacheManager.get(cacheKey);
            if (cachedBid) {
                logger.info(`竞价缓存命中: ${adUnitId}`);
                return cachedBid;
            }

            // 生成请求签名
            const timestamp = Date.now();
            const signature = this.securityService.generateRequestSignature({
                adUnitId,
                options,
                timestamp
            });

            // 发送请求（带重试）
            const response = await this.retryManager.execute(async () => {
                return await axios.post(`/bid/${adUnitId}`, options, {
                    headers: {
                        'X-Timestamp': timestamp,
                        'X-Signature': signature
                    }
                });
            });

            // 验证响应
            this.securityService.validateResponse(response);

            // 解密竞价结果
            const decryptedBid = this.securityService.decryptData(response.data);
            
            // 缓存竞价结果
            await this.cacheManager.set(cacheKey, decryptedBid, 60); // 缓存1分钟
            
            return decryptedBid;
        } catch (error) {
            logger.error(`竞价失败: ${error.message}`);
            throw error;
        }
    }

    async trackImpression(adId, platform) {
        this.checkInitialization();

        try {
            // 生成请求签名
            const timestamp = Date.now();
            const signature = this.securityService.generateRequestSignature({
                adId,
                platform,
                timestamp
            });

            // 发送请求（带重试）
            await this.retryManager.execute(async () => {
                return await axios.post(`/track/impression/${adId}`, { platform }, {
                    headers: {
                        'X-Timestamp': timestamp,
                        'X-Signature': signature
                    }
                });
            });
        } catch (error) {
            logger.error(`记录广告展示失败: ${error.message}`);
            throw error;
        }
    }

    async trackClick(adId, platform, revenue) {
        this.checkInitialization();

        try {
            // 生成请求签名
            const timestamp = Date.now();
            const signature = this.securityService.generateRequestSignature({
                adId,
                platform,
                revenue,
                timestamp
            });

            // 发送请求（带重试）
            await this.retryManager.execute(async () => {
                return await axios.post(`/track/click/${adId}`, { platform, revenue }, {
                    headers: {
                        'X-Timestamp': timestamp,
                        'X-Signature': signature
                    }
                });
            });
        } catch (error) {
            logger.error(`记录广告点击失败: ${error.message}`);
            throw error;
        }
    }

    // 生成缓存键
    generateCacheKey(type, adUnitId, options) {
        return `${type}:${adUnitId}:${JSON.stringify(options)}`;
    }

    validateConfig() {
        if (!this.config.apiUrl) {
            throw new Error('缺少API URL配置');
        }
        if (!this.config.appId) {
            throw new Error('缺少应用ID配置');
        }
        if (!this.config.appKey) {
            throw new Error('缺少应用密钥配置');
        }
    }

    checkInitialization() {
        if (!this.initialized) {
            throw new Error('SDK未初始化');
        }
    }
}

export default AdSDK; 