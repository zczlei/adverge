const crypto = require('crypto');
const jwt = require('jsonwebtoken');
const logger = require('../utils/logger');

class SecurityService {
    constructor() {
        this.secretKey = process.env.JWT_SECRET || crypto.randomBytes(32).toString('hex');
        this.tokenExpiration = '24h';
        this.rateLimitWindow = 60 * 1000; // 1分钟
        this.maxRequests = 100;
        this.ipBlacklist = new Set();
    }

    // 生成JWT令牌
    generateToken(payload) {
        try {
            return jwt.sign(payload, this.secretKey, { expiresIn: this.tokenExpiration });
        } catch (error) {
            logger.error('生成令牌失败:', error);
            throw new Error('令牌生成失败');
        }
    }

    // 验证JWT令牌
    verifyToken(token) {
        try {
            return jwt.verify(token, this.secretKey);
        } catch (error) {
            logger.error('验证令牌失败:', error);
            throw new Error('令牌无效或已过期');
        }
    }

    // 加密数据
    encryptData(data) {
        try {
            const iv = crypto.randomBytes(16);
            const cipher = crypto.createCipheriv('aes-256-cbc', Buffer.from(this.secretKey), iv);
            let encrypted = cipher.update(JSON.stringify(data));
            encrypted = Buffer.concat([encrypted, cipher.final()]);
            return {
                iv: iv.toString('hex'),
                encryptedData: encrypted.toString('hex')
            };
        } catch (error) {
            logger.error('加密数据失败:', error);
            throw new Error('数据加密失败');
        }
    }

    // 解密数据
    decryptData(encryptedData) {
        try {
            const iv = Buffer.from(encryptedData.iv, 'hex');
            const encryptedText = Buffer.from(encryptedData.encryptedData, 'hex');
            const decipher = crypto.createDecipheriv('aes-256-cbc', Buffer.from(this.secretKey), iv);
            let decrypted = decipher.update(encryptedText);
            decrypted = Buffer.concat([decrypted, decipher.final()]);
            return JSON.parse(decrypted.toString());
        } catch (error) {
            logger.error('解密数据失败:', error);
            throw new Error('数据解密失败');
        }
    }

    // 生成安全的随机字符串
    generateSecureRandom(length = 32) {
        return crypto.randomBytes(length).toString('hex');
    }

    // 验证请求来源
    validateRequestOrigin(req) {
        const origin = req.headers.origin;
        const allowedOrigins = process.env.ALLOWED_ORIGINS ? process.env.ALLOWED_ORIGINS.split(',') : [];
        
        if (!allowedOrigins.includes(origin)) {
            logger.warn(`非法请求来源: ${origin}`);
            throw new Error('非法请求来源');
        }
        return true;
    }

    // 检查IP是否在黑名单中
    isIpBlacklisted(ip) {
        return this.ipBlacklist.has(ip);
    }

    // 添加IP到黑名单
    addToBlacklist(ip) {
        this.ipBlacklist.add(ip);
        logger.warn(`IP已加入黑名单: ${ip}`);
    }

    // 从黑名单中移除IP
    removeFromBlacklist(ip) {
        this.ipBlacklist.delete(ip);
        logger.info(`IP已从黑名单移除: ${ip}`);
    }

    // 验证请求签名
    validateRequestSignature(req) {
        const signature = req.headers['x-signature'];
        const timestamp = req.headers['x-timestamp'];
        
        if (!signature || !timestamp) {
            throw new Error('缺少必要的请求头');
        }

        // 验证时间戳是否在有效范围内（5分钟内）
        const now = Date.now();
        if (Math.abs(now - parseInt(timestamp)) > 5 * 60 * 1000) {
            throw new Error('请求已过期');
        }

        // 验证签名
        const expectedSignature = this.generateRequestSignature(req, timestamp);
        if (signature !== expectedSignature) {
            throw new Error('签名验证失败');
        }

        return true;
    }

    // 生成请求签名
    generateRequestSignature(req, timestamp) {
        const data = {
            method: req.method,
            path: req.path,
            query: req.query,
            body: req.body,
            timestamp
        };
        
        const hmac = crypto.createHmac('sha256', this.secretKey);
        hmac.update(JSON.stringify(data));
        return hmac.digest('hex');
    }

    // 清理过期的黑名单IP
    cleanupBlacklist() {
        // 这里可以实现定期清理黑名单的逻辑
        // 例如：24小时后自动移除
    }
}

module.exports = new SecurityService(); 