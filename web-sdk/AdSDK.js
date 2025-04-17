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
        this.winningAN = null;
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

            this.initialized = true;
            logger.info('SDK初始化成功');
        } catch (error) {
            logger.error(`SDK初始化失败: ${error.message}`);
            throw error;
        }
    }

    // 发起广告填充请求
    async requestAdFill(adUnitId, options = {}) {
        this.checkInitialization();

        try {
            // 生成请求签名
            const timestamp = Date.now();
            const signature = this.securityService.generateRequestSignature({
                adUnitId,
                options,
                timestamp
            });

            // 发送广告填充请求
            const response = await this.retryManager.execute(async () => {
                return await axios.post(`/ad/fill`, {
                    adUnitId,
                    ...options
                }, {
                    headers: {
                        'X-Timestamp': timestamp,
                        'X-Signature': signature
                    }
                });
            });

            // 验证响应
            this.securityService.validateResponse(response);

            // 解析胜出 AN 信息
            const { winningAN, bidToken } = response.data;
            this.winningAN = winningAN;

            // 记录竞价结果
            logger.info(`广告填充请求成功，胜出AN: ${winningAN}`);

            return {
                winningAN,
                bidToken
            };
        } catch (error) {
            logger.error(`广告填充请求失败: ${error.message}`);
            throw error;
        }
    }

    // 加载胜出 AN 的广告
    async loadWinningAd(adUnitId, bidToken) {
        this.checkInitialization();

        if (!this.winningAN) {
            throw new Error('未找到胜出AN');
        }

        try {
            // 根据胜出 AN 选择对应的适配器
            const adapter = this.getAdapter(this.winningAN);
            if (!adapter) {
                throw new Error(`未找到AN适配器: ${this.winningAN}`);
            }

            // 调用胜出 AN 的广告加载接口
            const ad = await adapter.loadAd(adUnitId, bidToken);
            
            logger.info(`成功加载胜出AN广告: ${this.winningAN}`);
            return ad;
        } catch (error) {
            logger.error(`加载胜出AN广告失败: ${error.message}`);
            throw error;
        }
    }

    // 展示广告
    async showAd(containerId, ad) {
        this.checkInitialization();

        try {
            const container = document.getElementById(containerId);
            if (!container) {
                throw new Error(`未找到广告容器: ${containerId}`);
            }

            // 调用胜出 AN 的广告展示接口
            const adapter = this.getAdapter(this.winningAN);
            await adapter.showAd(container, ad);

            // 记录广告展示
            await this.trackImpression(ad.id, this.winningAN);
            
            logger.info(`广告展示成功: ${containerId}`);
        } catch (error) {
            logger.error(`广告展示失败: ${error.message}`);
            throw error;
        }
    }

    // 获取 AN 适配器
    getAdapter(anName) {
        return this.adapters.get(anName);
    }

    // 记录广告展示
    async trackImpression(adId, platform) {
        try {
            const timestamp = Date.now();
            const signature = this.securityService.generateRequestSignature({
                adId,
                platform,
                timestamp
            });

            await axios.post(`/track/impression/${adId}`, { platform }, {
                headers: {
                    'X-Timestamp': timestamp,
                    'X-Signature': signature
                }
            });
        } catch (error) {
            logger.error(`记录广告展示失败: ${error.message}`);
        }
    }

    // 记录广告点击
    async trackClick(adId, platform, revenue) {
        try {
            const timestamp = Date.now();
            const signature = this.securityService.generateRequestSignature({
                adId,
                platform,
                revenue,
                timestamp
            });

            await axios.post(`/track/click/${adId}`, { platform, revenue }, {
                headers: {
                    'X-Timestamp': timestamp,
                    'X-Signature': signature
                }
            });
        } catch (error) {
            logger.error(`记录广告点击失败: ${error.message}`);
        }
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