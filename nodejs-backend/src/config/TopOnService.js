const axios = require('axios');

class TopOnService {
  constructor(config) {
    this.apiUrl = config.apiUrl || 'https://api.toponadx.com';
    this.appId = config.appId;
    this.appKey = config.appKey;
    this.placementId = config.placementId;
  }

  async bid(placementId, userData) {
    try {
      const bidRequest = this.buildBidRequest(placementId, userData);
      
      const response = await axios.post(`${this.apiUrl}/bid`, bidRequest, {
        timeout: 100, // 100ms超时
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.appKey}`
        }
      });

      if (!response.data || !response.data.ads) {
        return null;
      }

      return {
        source: 'TopOn',
        price: response.data.price,
        creative: response.data.ads[0],
        render: () => this.render(response.data.ads[0])
      };
    } catch (error) {
      console.error('TopOn竞价请求失败:', error);
      return null;
    }
  }

  buildBidRequest(placementId, userData) {
    return {
      id: this.generateBidId(),
      imp: [{
        id: placementId,
        banner: {
          w: 300,
          h: 250
        }
      }],
      app: {
        id: this.appId,
        name: this.config.appName,
        bundle: this.config.bundleId
      },
      device: {
        ua: userData.userAgent,
        ip: userData.ip,
        geo: userData.geo,
        devicetype: userData.device,
        os: userData.os,
        osv: userData.osVersion,
        h: userData.screenHeight,
        w: userData.screenWidth,
        language: userData.language
      },
      user: {
        id: userData.userId,
        yob: userData.yob,
        gender: userData.gender,
        keywords: userData.keywords,
        interests: userData.interests
      },
      at: 1, // 第一价格竞价
      tmax: 100 // 100ms超时
    };
  }

  render(creative) {
    const container = document.createElement('div');
    container.className = 'topon-ad';
    
    if (creative.type === 'banner') {
      container.innerHTML = creative.html;
    } else if (creative.type === 'native') {
      container.innerHTML = `
        <div class="native-ad-content">
          <img src="${creative.imageUrl}" alt="${creative.title}">
          <h3>${creative.title}</h3>
          <p>${creative.description}</p>
          <div class="sponsored">Sponsored</div>
        </div>
      `;
    }
    
    // 添加点击追踪
    container.addEventListener('click', () => {
      this.trackClick(creative.clickUrl);
      window.open(creative.clickUrl, '_blank');
    });
    
    return container;
  }

  generateBidId() {
    return `topon_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  async trackClick(clickUrl) {
    try {
      await axios.get(clickUrl);
    } catch (error) {
      console.error('TopOn点击追踪失败:', error);
    }
  }
}

module.exports = TopOnService; 