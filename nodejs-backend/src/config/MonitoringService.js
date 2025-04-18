const logger = require('../utils/logger');
const axios = require('axios');

class MonitoringService {
    constructor() {
        this.metrics = {
            requests: 0,
            errors: 0,
            cacheHits: 0,
            cacheMisses: 0,
            bidRequests: 0,
            bidResponses: 0,
            adImpressions: 0,
            adClicks: 0,
            revenue: 0
        };

        this.performanceMetrics = new Map();
        this.errorMetrics = new Map();
        this.startTime = Date.now();
    }

    // 记录请求
    recordRequest() {
        this.metrics.requests++;
        this.logMetric('requests', this.metrics.requests);
    }

    // 记录错误
    recordError(error) {
        this.metrics.errors++;
        const errorType = error.name || 'unknown';
        this.errorMetrics.set(errorType, (this.errorMetrics.get(errorType) || 0) + 1);
        this.logMetric('errors', this.metrics.errors);
    }

    // 记录缓存命中
    recordCacheHit() {
        this.metrics.cacheHits++;
        this.logMetric('cacheHits', this.metrics.cacheHits);
    }

    // 记录缓存未命中
    recordCacheMiss() {
        this.metrics.cacheMisses++;
        this.logMetric('cacheMisses', this.metrics.cacheMisses);
    }

    // 记录竞价请求
    recordBidRequest() {
        this.metrics.bidRequests++;
        this.logMetric('bidRequests', this.metrics.bidRequests);
    }

    // 记录竞价响应
    recordBidResponse() {
        this.metrics.bidResponses++;
        this.logMetric('bidResponses', this.metrics.bidResponses);
    }

    // 记录广告展示
    recordAdImpression(adId, platform) {
        this.metrics.adImpressions++;
        this.logMetric('adImpressions', this.metrics.adImpressions);
        this.trackPlatformPerformance(platform, 'impressions');
    }

    // 记录广告点击
    recordAdClick(adId, platform) {
        this.metrics.adClicks++;
        this.logMetric('adClicks', this.metrics.adClicks);
        this.trackPlatformPerformance(platform, 'clicks');
    }

    // 记录收入
    recordRevenue(amount, platform) {
        this.metrics.revenue += amount;
        this.logMetric('revenue', this.metrics.revenue);
        this.trackPlatformPerformance(platform, 'revenue', amount);
    }

    // 记录性能指标
    recordPerformance(metric, value) {
        const current = this.performanceMetrics.get(metric) || [];
        current.push({
            value,
            timestamp: Date.now()
        });
        this.performanceMetrics.set(metric, current);
    }

    // 跟踪平台性能
    trackPlatformPerformance(platform, metric, value = 1) {
        const key = `platform:${platform}:${metric}`;
        const current = this.performanceMetrics.get(key) || [];
        current.push({
            value,
            timestamp: Date.now()
        });
        this.performanceMetrics.set(key, current);
    }

    // 获取性能报告
    getPerformanceReport() {
        const uptime = Date.now() - this.startTime;
        const report = {
            uptime,
            metrics: { ...this.metrics },
            errorDistribution: Object.fromEntries(this.errorMetrics),
            platformPerformance: {}
        };

        // 计算平台性能指标
        for (const [key, data] of this.performanceMetrics.entries()) {
            if (key.startsWith('platform:')) {
                const [_, platform, metric] = key.split(':');
                if (!report.platformPerformance[platform]) {
                    report.platformPerformance[platform] = {};
                }
                report.platformPerformance[platform][metric] = this.calculateMetrics(data);
            }
        }

        return report;
    }

    // 计算指标统计
    calculateMetrics(data) {
        const values = data.map(d => d.value);
        return {
            total: values.reduce((a, b) => a + b, 0),
            average: values.reduce((a, b) => a + b, 0) / values.length,
            min: Math.min(...values),
            max: Math.max(...values),
            count: values.length
        };
    }

    // 记录指标到日志
    logMetric(name, value) {
        logger.info(`Metric: ${name}=${value}`);
    }

    // 定期报告性能数据
    startReporting(interval = 60000) {
        setInterval(async () => {
            try {
                const report = this.getPerformanceReport();
                await this.sendReport(report);
            } catch (error) {
                logger.error('发送性能报告失败:', error);
            }
        }, interval);
    }

    // 发送报告到监控系统
    async sendReport(report) {
        try {
            await axios.post(process.env.MONITORING_API_URL, report, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${process.env.MONITORING_API_KEY}`
                }
            });
        } catch (error) {
            logger.error('发送监控报告失败:', error);
        }
    }
}

module.exports = new MonitoringService(); 