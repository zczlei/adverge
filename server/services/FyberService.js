const axios = require('axios');

class FyberService {
  constructor(config) {
    this.apiUrl = config.apiUrl || 'https://api.fyber.com/v1';
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
          'Authorization': `Bearer ${this.appKey}`,
          'X-FB-App-ID': this.appId
        },
        timeout: 100 // 100ms超时
      });

      if (response.data && response.data.bid) {
        return {
          price: response.data.bid.price,
          creative: response.data.bid.creative,
          adId: response.data.bid.adId,
          platform: 'Fyber'
        };
      }
      return null;
    } catch (error) {
      console.error('Fyber竞价失败:', error);
      return null;
    }
  }

  buildBidRequest(placementId, userData) {
    return {
      app: {
        id: this.appId,
        name: userData.appName,
        bundle: userData.appBundle,
        ver: userData.appVersion
      },
      device: {
        ua: userData.userAgent,
        ip: userData.ip,
        geo: {
          country: userData.country,
          region: userData.region,
          city: userData.city,
          lat: userData.latitude,
          lon: userData.longitude
        },
        deviceType: userData.deviceType,
        os: userData.os,
        osv: userData.osVersion,
        connectionType: userData.connectionType,
        carrier: userData.carrier,
        language: userData.language,
        screen: {
          w: userData.screenWidth,
          h: userData.screenHeight
        }
      },
      user: {
        id: userData.userId,
        yob: userData.yob,
        gender: userData.gender,
        interests: userData.interests
      },
      imp: [{
        id: this.generateBidId(),
        placementId: placementId,
        banner: {
          w: userData.adSize.width,
          h: userData.adSize.height,
          pos: userData.position
        }
      }],
      at: 1, // 1 = 首次价格拍卖
      tmax: 100 // 100ms超时
    };
  }

  render(adData) {
    const container = document.createElement('div');
    container.className = 'fyber-ads-container';
    container.style.width = '100%';
    container.style.height = '100%';
    container.style.position = 'relative';

    // 创建广告内容容器
    const adContent = document.createElement('div');
    adContent.className = 'fyber-ad-content';
    adContent.style.width = '100%';
    adContent.style.height = '100%';
    adContent.style.position = 'relative';

    // 添加标题
    if (adData.creative.title) {
      const title = document.createElement('h3');
      title.textContent = adData.creative.title;
      title.style.margin = '0';
      title.style.padding = '10px';
      title.style.color = '#333';
      adContent.appendChild(title);
    }

    // 添加图片
    if (adData.creative.imageUrl) {
      const image = document.createElement('img');
      image.src = adData.creative.imageUrl;
      image.style.width = '100%';
      image.style.height = 'auto';
      image.style.display = 'block';
      adContent.appendChild(image);
    }

    // 添加描述
    if (adData.creative.description) {
      const description = document.createElement('p');
      description.textContent = adData.creative.description;
      description.style.margin = '10px';
      description.style.padding = '0';
      description.style.color = '#666';
      adContent.appendChild(description);
    }

    // 添加行动按钮
    if (adData.creative.cta) {
      const ctaButton = document.createElement('button');
      ctaButton.textContent = adData.creative.cta;
      ctaButton.style.margin = '10px';
      ctaButton.style.padding = '8px 16px';
      ctaButton.style.backgroundColor = '#007bff';
      ctaButton.style.color = 'white';
      ctaButton.style.border = 'none';
      ctaButton.style.borderRadius = '4px';
      ctaButton.style.cursor = 'pointer';
      adContent.appendChild(ctaButton);
    }

    // 添加点击追踪
    adContent.onclick = () => {
      this.trackClick(adData.adId);
      window.open(adData.creative.clickUrl, '_blank');
    };

    container.appendChild(adContent);
    return container;
  }

  generateBidId() {
    return `fyber_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  async trackClick(adId) {
    try {
      await axios.post(`${this.apiUrl}/click`, {
        adId: adId,
        timestamp: Date.now()
      }, {
        headers: {
          'Authorization': `Bearer ${this.appKey}`,
          'X-FB-App-ID': this.appId
        }
      });
    } catch (error) {
      console.error('Fyber点击追踪失败:', error);
    }
  }
}

module.exports = FyberService; 