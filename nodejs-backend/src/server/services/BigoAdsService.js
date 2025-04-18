const axios = require('axios');

class BigoAdsService {
  constructor(config) {
    this.apiUrl = config.apiUrl || 'https://api.bigoads.com/v1';
    this.appId = config.appId;
    this.appKey = config.appKey;
    this.placementId = config.placementId;
  }

  async bid(placementId, userData) {
    try {
      const bidRequest = this.buildBidRequest(placementId, userData);
      
      const response = await axios.post(`${this.apiUrl}/bid`, bidRequest, {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.appKey}`
        },
        timeout: 100 // 100ms超时
      });

      if (response.data && response.data.bid) {
        return {
          price: response.data.bid.price,
          creative: response.data.bid.creative,
          adId: response.data.bid.adId,
          platform: 'BigoAds'
        };
      }
      return null;
    } catch (error) {
      console.error('Bigo Ads竞价失败:', error);
      return null;
    }
  }

  buildBidRequest(placementId, userData) {
    return {
      app: {
        id: this.appId,
        name: userData.appName,
        bundle: userData.appBundle
      },
      device: {
        ua: userData.userAgent,
        ip: userData.ip,
        geo: {
          country: userData.country,
          region: userData.region,
          city: userData.city
        },
        deviceType: userData.deviceType,
        os: userData.os,
        osv: userData.osVersion,
        connectionType: userData.connectionType
      },
      user: {
        id: userData.userId,
        yob: userData.yob,
        gender: userData.gender
      },
      imp: [{
        id: this.generateBidId(),
        placementId: placementId,
        banner: {
          w: userData.adSize.width,
          h: userData.adSize.height
        }
      }],
      at: 1, // 1 = 首次价格拍卖
      tmax: 100 // 100ms超时
    };
  }

  render(adData) {
    const container = document.createElement('div');
    container.className = 'bigo-ads-container';
    container.style.width = '100%';
    container.style.height = '100%';
    container.style.position = 'relative';

    // 创建iframe来加载广告内容
    const iframe = document.createElement('iframe');
    iframe.src = adData.creative.url;
    iframe.style.width = '100%';
    iframe.style.height = '100%';
    iframe.style.border = 'none';
    iframe.style.overflow = 'hidden';
    iframe.setAttribute('scrolling', 'no');

    // 添加点击追踪
    iframe.onload = () => {
      this.trackClick(adData.adId);
    };

    container.appendChild(iframe);
    return container;
  }

  generateBidId() {
    return `bigo_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  async trackClick(adId) {
    try {
      await axios.post(`${this.apiUrl}/click`, {
        adId: adId,
        timestamp: Date.now()
      }, {
        headers: {
          'Authorization': `Bearer ${this.appKey}`
        }
      });
    } catch (error) {
      console.error('Bigo Ads点击追踪失败:', error);
    }
  }
}

module.exports = BigoAdsService; 