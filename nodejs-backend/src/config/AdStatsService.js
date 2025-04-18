const fs = require('fs').promises;
const path = require('path');

class AdStatsService {
  constructor() {
    this.statsFile = path.join(__dirname, '../data/ad-stats.json');
    this.stats = {
      impressions: {},
      clicks: {},
      revenue: {},
      platforms: {},
      adTypes: {},
      devices: {},
      lastUpdated: new Date().toISOString()
    };
    this.loadStats();
  }

  async loadStats() {
    try {
      const data = await fs.readFile(this.statsFile, 'utf8');
      this.stats = JSON.parse(data);
    } catch (error) {
      console.log('初始化新的统计数据文件');
      await this.saveStats();
    }
  }

  async saveStats() {
    try {
      await fs.writeFile(this.statsFile, JSON.stringify(this.stats, null, 2));
    } catch (error) {
      console.error('保存统计数据失败:', error);
    }
  }

  async recordImpression(platform, adType, deviceType, price) {
    const date = new Date().toISOString().split('T')[0];
    
    // 更新平台统计
    this.stats.platforms[platform] = this.stats.platforms[platform] || { impressions: 0, clicks: 0, revenue: 0 };
    this.stats.platforms[platform].impressions++;
    this.stats.platforms[platform].revenue += price || 0;

    // 更新广告类型统计
    this.stats.adTypes[adType] = this.stats.adTypes[adType] || { impressions: 0, clicks: 0, revenue: 0 };
    this.stats.adTypes[adType].impressions++;
    this.stats.adTypes[adType].revenue += price || 0;

    // 更新设备类型统计
    this.stats.devices[deviceType] = this.stats.devices[deviceType] || { impressions: 0, clicks: 0, revenue: 0 };
    this.stats.devices[deviceType].impressions++;
    this.stats.devices[deviceType].revenue += price || 0;

    // 更新每日统计
    this.stats.impressions[date] = this.stats.impressions[date] || { total: 0, byPlatform: {}, byAdType: {}, byDevice: {} };
    this.stats.impressions[date].total++;
    this.stats.impressions[date].byPlatform[platform] = (this.stats.impressions[date].byPlatform[platform] || 0) + 1;
    this.stats.impressions[date].byAdType[adType] = (this.stats.impressions[date].byAdType[adType] || 0) + 1;
    this.stats.impressions[date].byDevice[deviceType] = (this.stats.impressions[date].byDevice[deviceType] || 0) + 1;

    this.stats.lastUpdated = new Date().toISOString();
    await this.saveStats();
  }

  async recordClick(platform, adType, deviceType) {
    const date = new Date().toISOString().split('T')[0];
    
    // 更新平台统计
    this.stats.platforms[platform] = this.stats.platforms[platform] || { impressions: 0, clicks: 0, revenue: 0 };
    this.stats.platforms[platform].clicks++;

    // 更新广告类型统计
    this.stats.adTypes[adType] = this.stats.adTypes[adType] || { impressions: 0, clicks: 0, revenue: 0 };
    this.stats.adTypes[adType].clicks++;

    // 更新设备类型统计
    this.stats.devices[deviceType] = this.stats.devices[deviceType] || { impressions: 0, clicks: 0, revenue: 0 };
    this.stats.devices[deviceType].clicks++;

    // 更新每日统计
    this.stats.clicks[date] = this.stats.clicks[date] || { total: 0, byPlatform: {}, byAdType: {}, byDevice: {} };
    this.stats.clicks[date].total++;
    this.stats.clicks[date].byPlatform[platform] = (this.stats.clicks[date].byPlatform[platform] || 0) + 1;
    this.stats.clicks[date].byAdType[adType] = (this.stats.clicks[date].byAdType[adType] || 0) + 1;
    this.stats.clicks[date].byDevice[deviceType] = (this.stats.clicks[date].byDevice[deviceType] || 0) + 1;

    this.stats.lastUpdated = new Date().toISOString();
    await this.saveStats();
  }

  async getStats(startDate, endDate) {
    const stats = {
      total: {
        impressions: 0,
        clicks: 0,
        revenue: 0,
        ctr: 0
      },
      byPlatform: {},
      byAdType: {},
      byDevice: {},
      daily: []
    };

    // 计算日期范围内的统计数据
    const currentDate = new Date(startDate);
    const end = new Date(endDate);

    while (currentDate <= end) {
      const date = currentDate.toISOString().split('T')[0];
      
      // 添加每日数据
      stats.daily.push({
        date,
        impressions: this.stats.impressions[date]?.total || 0,
        clicks: this.stats.clicks[date]?.total || 0,
        revenue: this.calculateDailyRevenue(date)
      });

      // 累加平台统计
      if (this.stats.impressions[date]?.byPlatform) {
        Object.entries(this.stats.impressions[date].byPlatform).forEach(([platform, count]) => {
          stats.byPlatform[platform] = stats.byPlatform[platform] || { impressions: 0, clicks: 0, revenue: 0 };
          stats.byPlatform[platform].impressions += count;
          stats.byPlatform[platform].clicks += this.stats.clicks[date]?.byPlatform[platform] || 0;
          stats.byPlatform[platform].revenue += this.calculatePlatformRevenue(platform, date);
        });
      }

      // 累加广告类型统计
      if (this.stats.impressions[date]?.byAdType) {
        Object.entries(this.stats.impressions[date].byAdType).forEach(([adType, count]) => {
          stats.byAdType[adType] = stats.byAdType[adType] || { impressions: 0, clicks: 0, revenue: 0 };
          stats.byAdType[adType].impressions += count;
          stats.byAdType[adType].clicks += this.stats.clicks[date]?.byAdType[adType] || 0;
          stats.byAdType[adType].revenue += this.calculateAdTypeRevenue(adType, date);
        });
      }

      // 累加设备类型统计
      if (this.stats.impressions[date]?.byDevice) {
        Object.entries(this.stats.impressions[date].byDevice).forEach(([deviceType, count]) => {
          stats.byDevice[deviceType] = stats.byDevice[deviceType] || { impressions: 0, clicks: 0, revenue: 0 };
          stats.byDevice[deviceType].impressions += count;
          stats.byDevice[deviceType].clicks += this.stats.clicks[date]?.byDevice[deviceType] || 0;
          stats.byDevice[deviceType].revenue += this.calculateDeviceRevenue(deviceType, date);
        });
      }

      currentDate.setDate(currentDate.getDate() + 1);
    }

    // 计算总计
    Object.values(stats.byPlatform).forEach(platform => {
      stats.total.impressions += platform.impressions;
      stats.total.clicks += platform.clicks;
      stats.total.revenue += platform.revenue;
    });

    // 计算点击率
    if (stats.total.impressions > 0) {
      stats.total.ctr = (stats.total.clicks / stats.total.impressions) * 100;
    }

    return stats;
  }

  calculateDailyRevenue(date) {
    let revenue = 0;
    Object.values(this.stats.platforms).forEach(platform => {
      revenue += platform.revenue || 0;
    });
    return revenue;
  }

  calculatePlatformRevenue(platform, date) {
    return this.stats.platforms[platform]?.revenue || 0;
  }

  calculateAdTypeRevenue(adType, date) {
    return this.stats.adTypes[adType]?.revenue || 0;
  }

  calculateDeviceRevenue(deviceType, date) {
    return this.stats.devices[deviceType]?.revenue || 0;
  }
}

module.exports = new AdStatsService(); 