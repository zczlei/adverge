const axios = require('axios');

class MintegralService {
  constructor(config) {
    this.apiUrl = config.apiUrl || 'https://api.mintegral.com/v1';
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
          'X-MT-App-ID': this.appId
        },
        timeout: 100 // 100ms超时
      });

      if (response.data && response.data.bid) {
        return {
          price: response.data.bid.price,
          creative: response.data.bid.creative,
          adId: response.data.bid.adId,
          platform: 'Mintegral'
        };
      }
      return null;
    } catch (error) {
      console.error('Mintegral竞价失败:', error);
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
        language: userData.language
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
    container.className = 'mintegral-ads-container';
    container.style.width = '100%';
    container.style.height = '100%';
    container.style.position = 'relative';

    // 创建视频播放器容器
    const videoContainer = document.createElement('div');
    videoContainer.className = 'mintegral-video-container';
    videoContainer.style.width = '100%';
    videoContainer.style.height = '100%';
    videoContainer.style.position = 'relative';

    // 创建视频元素
    const video = document.createElement('video');
    video.src = adData.creative.videoUrl;
    video.style.width = '100%';
    video.style.height = '100%';
    video.controls = true;
    video.autoplay = true;
    video.muted = true;

    // 添加跳过按钮
    const skipButton = document.createElement('button');
    skipButton.className = 'mintegral-skip-button';
    skipButton.textContent = '跳过广告';
    skipButton.style.position = 'absolute';
    skipButton.style.right = '10px';
    skipButton.style.bottom = '10px';
    skipButton.style.padding = '5px 10px';
    skipButton.style.backgroundColor = 'rgba(0,0,0,0.5)';
    skipButton.style.color = 'white';
    skipButton.style.border = 'none';
    skipButton.style.borderRadius = '3px';
    skipButton.style.cursor = 'pointer';

    // 添加点击追踪
    videoContainer.onclick = () => {
      this.trackClick(adData.adId);
      window.open(adData.creative.clickUrl, '_blank');
    };

    videoContainer.appendChild(video);
    videoContainer.appendChild(skipButton);
    container.appendChild(videoContainer);
    return container;
  }

  generateBidId() {
    return `mintegral_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  async trackClick(adId) {
    try {
      await axios.post(`${this.apiUrl}/click`, {
        adId: adId,
        timestamp: Date.now()
      }, {
        headers: {
          'Authorization': `Bearer ${this.appKey}`,
          'X-MT-App-ID': this.appId
        }
      });
    } catch (error) {
      console.error('Mintegral点击追踪失败:', error);
    }
  }
}

module.exports = MintegralService; 