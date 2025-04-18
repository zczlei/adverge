const TopOnService = require('./TopOnService');
const BigoAdsService = require('./BigoAdsService');
const InMobiService = require('./InMobiService');
const MintegralService = require('./MintegralService');
const VungleService = require('./VungleService');
const FyberService = require('./FyberService');
const ChartboostService = require('./ChartboostService');
const IronSourceService = require('./IronSourceService');
const UnityAdsService = require('./UnityAdsService');
const MahimetaService = require('./MahimetaService');
const logger = require('../utils/logger');

class BiddingService {
  constructor(config) {
    this.services = {
      TopOn: new TopOnService(config.topon),
      BigoAds: new BigoAdsService(config.bigoAds),
      InMobi: new InMobiService(config.inMobi),
      Mintegral: new MintegralService(config.mintegral),
      Vungle: new VungleService(config.vungle),
      Fyber: new FyberService(config.fyber),
      Chartboost: new ChartboostService(config.chartboost),
      IronSource: new IronSourceService(config.ironSource),
      UnityAds: new UnityAdsService(config.unityAds),
      Mahimeta: new MahimetaService(config.mahimeta)
    };
    
    this.redis = config.redis;
    this.metrics = config.metrics;
  }

  async processBid(placementId, userData) {
    logger.info(`开始处理竞价请求: ${placementId}`);
    
    try {
      // 1. 检查缓存
      const cachedBid = await this.redis.get(`bid:${placementId}`);
      if (cachedBid) {
        logger.info(`从缓存获取竞价结果: ${placementId}`);
        return JSON.parse(cachedBid);
      }

      // 2. 向所有支持bidding的平台发起询价
      logger.info(`向各平台发起询价: ${placementId}`);
      const bids = await Promise.all(
        Object.entries(this.services).map(async ([platform, service]) => {
          try {
            const bid = await service.bid(placementId, userData);
            if (bid) {
              logger.info(`收到${platform}竞价响应: ${bid.price}`);
              // 记录竞价数据
              await this.metrics.recordBid({
                placementId,
                platform,
                price: bid.price,
                timestamp: Date.now()
              });
            }
            return bid;
          } catch (error) {
            logger.error(`${platform}竞价失败:`, error);
            return null;
          }
        })
      );

      // 3. 选择最高出价
      logger.info(`选择最高出价: ${placementId}`);
      const winningBid = bids.reduce((max, bid) => 
        bid && bid.price > (max?.price || 0) ? bid : max, null
      );

      if (winningBid) {
        // 4. 通知胜出平台
        logger.info(`通知胜出平台: ${winningBid.platform}`);
        await this.notifyWinningNetwork(winningBid);
        
        // 5. 缓存结果
        await this.redis.setex(
          `bid:${placementId}`,
          300, // 5分钟过期
          JSON.stringify(winningBid)
        );
      }

      return winningBid;
    } catch (error) {
      logger.error('竞价处理失败:', error);
      return null;
    }
  }

  async notifyWinningNetwork(winningBid) {
    const { platform, bidToken } = winningBid;
    const service = this.services[platform];
    
    if (service) {
      try {
        await service.notifyWin(bidToken);
        logger.info(`成功通知${platform}胜出`);
      } catch (error) {
        logger.error(`通知${platform}胜出失败:`, error);
      }
    }
  }

  async recordImpression(placementId, platform, price) {
    try {
      await this.metrics.recordImpression({
        placementId,
        platform,
        price,
        timestamp: Date.now()
      });
      logger.info(`记录广告展示: ${placementId}`);
    } catch (error) {
      logger.error('展示记录失败:', error);
    }
  }

  async recordClick(placementId, platform) {
    try {
      await this.metrics.recordClick({
        placementId,
        platform,
        timestamp: Date.now()
      });
      logger.info(`记录广告点击: ${placementId}`);
    } catch (error) {
      logger.error('点击记录失败:', error);
    }
  }
}

module.exports = BiddingService; 