const express = require('express');
const router = express.Router();
const AdConfigManager = require('../config/AdConfigManager');
const AdStatsService = require('../services/AdStatsService');
const AdReportService = require('../services/AdReportService');
const TopOnService = require('../services/TopOnService');
const BigoAdsService = require('../services/BigoAdsService');
const InMobiService = require('../services/InMobiService');
const MintegralService = require('../services/MintegralService');
const VungleService = require('../services/VungleService');
const FyberService = require('../services/FyberService');
const ChartboostService = require('../services/ChartboostService');
const IronSourceService = require('../services/IronSourceService');
const UnityAdsService = require('../services/UnityAdsService');
const MahimetaService = require('../services/MahimetaService');

class AdRequestRouter {
  constructor() {
    this.router = router;
    this.setupRoutes();
    this.services = {
      topon: new TopOnService(),
      bigo: new BigoAdsService(),
      inmobi: new InMobiService(),
      mintegral: new MintegralService(),
      vungle: new VungleService(),
      fyber: new FyberService(),
      chartboost: new ChartboostService(),
      ironsource: new IronSourceService(),
      unity: new UnityAdsService(),
      mahimeta: new MahimetaService()
    };
  }

  setupRoutes() {
    // 获取广告请求
    this.router.get('/ad', async (req, res) => {
      try {
        const { adType, platform } = req.query;
        
        if (!adType || !platform) {
          return res.status(400).json({ error: '缺少必要参数' });
        }

        // 检查平台是否启用
        if (!AdConfigManager.isPlatformEnabled(platform)) {
          return res.status(400).json({ error: '平台未启用' });
        }

        // 获取平台服务
        const service = this.services[platform];
        if (!service) {
          return res.status(400).json({ error: '不支持的广告平台' });
        }

        // 获取广告位ID
        const placementId = AdConfigManager.getPlacementId(platform, adType);
        if (!placementId) {
          return res.status(400).json({ error: '无效的广告位ID' });
        }

        // 构建广告请求
        const bidRequest = {
          placementId,
          adType,
          deviceInfo: this.getDeviceInfo(req),
          userInfo: this.getUserInfo(req)
        };

        // 发送竞价请求
        const bidResponse = await service.bid(bidRequest);
        
        if (!bidResponse) {
          return res.status(404).json({ error: '未找到合适的广告' });
        }

        // 记录广告展示
        await AdStatsService.recordImpression(
          platform,
          adType,
          bidRequest.deviceInfo.deviceType,
          bidResponse.price
        );

        // 返回广告内容
        res.json({
          success: true,
          data: {
            adType,
            platform,
            creative: bidResponse.creative,
            price: bidResponse.price,
            currency: bidResponse.currency,
            tracking: {
              impression: bidResponse.impressionUrl,
              click: bidResponse.clickUrl
            }
          }
        });

      } catch (error) {
        console.error('广告请求处理失败:', error);
        res.status(500).json({ error: '服务器内部错误' });
      }
    });

    // 广告点击跟踪
    this.router.get('/track/click', async (req, res) => {
      try {
        const { platform, bidId, adType, deviceType } = req.query;
        
        if (!platform || !bidId || !adType || !deviceType) {
          return res.status(400).json({ error: '缺少必要参数' });
        }

        const service = this.services[platform];
        if (!service) {
          return res.status(400).json({ error: '不支持的广告平台' });
        }

        // 记录广告点击
        await AdStatsService.recordClick(platform, adType, deviceType);

        // 调用平台点击跟踪
        await service.trackClick(bidId);
        res.json({ success: true });

      } catch (error) {
        console.error('点击跟踪失败:', error);
        res.status(500).json({ error: '服务器内部错误' });
      }
    });

    // 广告展示跟踪
    this.router.get('/track/impression', async (req, res) => {
      try {
        const { platform, bidId } = req.query;
        
        if (!platform || !bidId) {
          return res.status(400).json({ error: '缺少必要参数' });
        }

        const service = this.services[platform];
        if (!service) {
          return res.status(400).json({ error: '不支持的广告平台' });
        }

        await service.trackImpression(bidId);
        res.json({ success: true });

      } catch (error) {
        console.error('展示跟踪失败:', error);
        res.status(500).json({ error: '服务器内部错误' });
      }
    });

    // 获取统计数据
    this.router.get('/stats', async (req, res) => {
      try {
        const { startDate, endDate } = req.query;
        
        if (!startDate || !endDate) {
          return res.status(400).json({ error: '缺少必要参数' });
        }

        const stats = await AdStatsService.getStats(startDate, endDate);
        res.json({ success: true, data: stats });

      } catch (error) {
        console.error('获取统计数据失败:', error);
        res.status(500).json({ error: '服务器内部错误' });
      }
    });

    // 生成报表
    this.router.post('/reports/generate', async (req, res) => {
      try {
        const { startDate, endDate } = req.body;
        
        if (!startDate || !endDate) {
          return res.status(400).json({ error: '缺少必要参数' });
        }

        const report = await AdReportService.generateDateRangeReport(startDate, endDate);
        res.json({ success: true, data: report });

      } catch (error) {
        console.error('生成报表失败:', error);
        res.status(500).json({ error: '服务器内部错误' });
      }
    });

    // 获取报表列表
    this.router.get('/reports', async (req, res) => {
      try {
        const reports = await AdReportService.listReports();
        res.json({ success: true, data: reports });

      } catch (error) {
        console.error('获取报表列表失败:', error);
        res.status(500).json({ error: '服务器内部错误' });
      }
    });

    // 获取指定报表
    this.router.get('/reports/:filename', async (req, res) => {
      try {
        const { filename } = req.params;
        const report = await AdReportService.getReport(filename);
        
        if (!report) {
          return res.status(404).json({ error: '报表不存在' });
        }

        res.json({ success: true, data: report });

      } catch (error) {
        console.error('获取报表失败:', error);
        res.status(500).json({ error: '服务器内部错误' });
      }
    });

    // 下载报表
    this.router.get('/reports/:filename/download', async (req, res) => {
      try {
        const { filename } = req.params;
        const { format = 'excel' } = req.query;
        
        const filePath = path.join(__dirname, '../data/reports', `${filename}.${format === 'excel' ? 'xlsx' : 'json'}`);
        
        if (!fs.existsSync(filePath)) {
          return res.status(404).json({ error: '报表文件不存在' });
        }

        res.download(filePath);

      } catch (error) {
        console.error('下载报表失败:', error);
        res.status(500).json({ error: '服务器内部错误' });
      }
    });
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
      userId: req.query.userId || 'anonymous',
      age: req.query.age || null,
      gender: req.query.gender || null,
      interests: req.query.interests ? req.query.interests.split(',') : []
    };
  }

  detectDeviceType(userAgent) {
    if (/mobile/i.test(userAgent)) {
      return 'mobile';
    } else if (/tablet/i.test(userAgent)) {
      return 'tablet';
    } else {
      return 'desktop';
    }
  }
}

module.exports = new AdRequestRouter().router; 