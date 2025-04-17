import CryptoJS from 'crypto-js';
import { logger } from '../utils/logger';

export class SecurityService {
    constructor(config) {
        this.config = config;
        this.initialized = false;
        this.encryptionKey = null;
    }

    async initialize() {
        try {
            // 从配置中获取加密密钥
            this.encryptionKey = this.config.encryptionKey;
            if (!this.encryptionKey) {
                throw new Error('缺少加密密钥配置');
            }

            this.initialized = true;
            logger.info('安全服务初始化成功');
        } catch (error) {
            logger.error(`安全服务初始化失败: ${error.message}`);
            throw error;
        }
    }

    generateRequestSignature(data) {
        this.checkInitialization();

        try {
            // 将数据转换为字符串
            const dataString = JSON.stringify(data);
            
            // 使用HMAC-SHA256生成签名
            const signature = CryptoJS.HmacSHA256(dataString, this.config.appKey).toString();
            
            return signature;
        } catch (error) {
            logger.error(`生成请求签名失败: ${error.message}`);
            throw error;
        }
    }

    encryptData(data) {
        this.checkInitialization();

        try {
            // 将数据转换为字符串
            const dataString = JSON.stringify(data);
            
            // 使用AES加密数据
            const encrypted = CryptoJS.AES.encrypt(dataString, this.encryptionKey).toString();
            
            return encrypted;
        } catch (error) {
            logger.error(`加密数据失败: ${error.message}`);
            throw error;
        }
    }

    decryptData(encryptedData) {
        this.checkInitialization();

        try {
            // 使用AES解密数据
            const decrypted = CryptoJS.AES.decrypt(encryptedData, this.encryptionKey);
            const dataString = decrypted.toString(CryptoJS.enc.Utf8);
            
            // 解析JSON数据
            const data = JSON.parse(dataString);
            
            return data;
        } catch (error) {
            logger.error(`解密数据失败: ${error.message}`);
            throw error;
        }
    }

    validateResponse(response) {
        this.checkInitialization();

        try {
            // 获取响应头中的签名和时间戳
            const signature = response.headers['x-signature'];
            const timestamp = response.headers['x-timestamp'];
            
            if (!signature || !timestamp) {
                throw new Error('响应缺少签名或时间戳');
            }

            // 验证时间戳是否在有效范围内（例如5分钟内）
            const now = Date.now();
            const responseTime = parseInt(timestamp);
            if (Math.abs(now - responseTime) > 5 * 60 * 1000) {
                throw new Error('响应时间戳已过期');
            }

            // 验证签名
            const expectedSignature = this.generateRequestSignature({
                data: response.data,
                timestamp: responseTime
            });

            if (signature !== expectedSignature) {
                throw new Error('响应签名验证失败');
            }

            return true;
        } catch (error) {
            logger.error(`验证响应失败: ${error.message}`);
            throw error;
        }
    }

    checkInitialization() {
        if (!this.initialized) {
            throw new Error('安全服务未初始化');
        }
    }
} 