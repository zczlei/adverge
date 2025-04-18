const express = require('express');
const router = express.Router();
const adService = require('../services/AdService');
const securityService = require('../services/SecurityService');
const logger = require('../utils/logger');

// 添加请求签名中间件
const requireSignature = (req, res, next) => {
    try {
        const timestamp = Date.now();
        const signature = securityService.generateRequestSignature(req, timestamp);
        
        // 设置响应头
        res.setHeader('X-Timestamp', timestamp);
        res.setHeader('X-Signature', signature);
        
        next();
    } catch (error) {
        logger.error(`生成请求签名失败: ${error.message}`);
        res.status(500).json({ error: '服务器内部错误' });
    }
};

// 获取广告
router.get('/ad/:adUnitId', requireSignature, async (req, res) => {
    try {
        const { adUnitId } = req.params;
        const options = req.query;
        
        const ad = await adService.getAd(adUnitId, options, req);
        res.json(ad);
    } catch (error) {
        logger.error(`获取广告失败: ${error.message}`);
        res.status(400).json({ error: error.message });
    }
});

// 竞价请求
router.post('/bid/:adUnitId', requireSignature, async (req, res) => {
    try {
        const { adUnitId } = req.params;
        const options = req.body;
        
        const bidResult = await adService.bid(adUnitId, options, req);
        res.json(bidResult);
    } catch (error) {
        logger.error(`竞价失败: ${error.message}`);
        res.status(400).json({ error: error.message });
    }
});

// 记录广告展示
router.post('/track/impression/:adId', requireSignature, async (req, res) => {
    try {
        const { adId } = req.params;
        const { platform } = req.body;
        
        await adService.trackImpression(adId, platform, req);
        res.json({ success: true });
    } catch (error) {
        logger.error(`记录广告展示失败: ${error.message}`);
        res.status(400).json({ error: error.message });
    }
});

// 记录广告点击
router.post('/track/click/:adId', requireSignature, async (req, res) => {
    try {
        const { adId } = req.params;
        const { platform, revenue } = req.body;
        
        await adService.trackClick(adId, platform, revenue, req);
        res.json({ success: true });
    } catch (error) {
        logger.error(`记录广告点击失败: ${error.message}`);
        res.status(400).json({ error: error.message });
    }
});

// 错误处理中间件
router.use((err, req, res, next) => {
    logger.error(`路由错误: ${err.message}`);
    res.status(500).json({ error: '服务器内部错误' });
});

module.exports = router; 