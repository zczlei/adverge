const axios = require('axios');

class ChartboostService {
  constructor(config) {
    this.apiUrl = config.apiUrl || 'https://api.chartboost.com/v1';
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
          'X-CB-App-ID': this.appId
        },
        timeout: 100 // 100ms超时
      });

      if (response.data && response.data.bid) {
        return {
          price: response.data.bid.price,
          creative: response.data.bid.creative,
          adId: response.data.bid.adId,
          platform: 'Chartboost'
        };
      }
      return null;
    } catch (error) {
      console.error('Chartboost竞价失败:', error);
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
    container.className = 'chartboost-ads-container';
    container.style.width = '100%';
    container.style.height = '100%';
    container.style.position = 'relative';

    // 创建视频播放器容器
    const videoContainer = document.createElement('div');
    videoContainer.className = 'chartboost-video-container';
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
    skipButton.className = 'chartboost-skip-button';
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

    // 添加进度条
    const progressBar = document.createElement('div');
    progressBar.className = 'chartboost-progress-bar';
    progressBar.style.position = 'absolute';
    progressBar.style.bottom = '0';
    progressBar.style.left = '0';
    progressBar.style.width = '100%';
    progressBar.style.height = '3px';
    progressBar.style.backgroundColor = 'rgba(255,255,255,0.3)';

    const progress = document.createElement('div');
    progress.className = 'chartboost-progress';
    progress.style.width = '0%';
    progress.style.height = '100%';
    progress.style.backgroundColor = '#00ff00';
    progressBar.appendChild(progress);

    // 更新进度条
    video.ontimeupdate = () => {
      const percent = (video.currentTime / video.duration) * 100;
      progress.style.width = `${percent}%`;
    };

    // 添加点击追踪
    videoContainer.onclick = () => {
      this.trackClick(adData.adId);
      window.open(adData.creative.clickUrl, '_blank');
    };

    videoContainer.appendChild(video);
    videoContainer.appendChild(skipButton);
    videoContainer.appendChild(progressBar);
    container.appendChild(videoContainer);
    return container;
  }

  generateBidId() {
    return `chartboost_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  async trackClick(adId) {
    try {
      await axios.post(`${this.apiUrl}/click`, {
        adId: adId,
        timestamp: Date.now()
      }, {
        headers: {
          'Authorization': `Bearer ${this.appKey}`,
          'X-CB-App-ID': this.appId
        }
      });
    } catch (error) {
      console.error('Chartboost点击追踪失败:', error);
    }
  }
}

module.exports = ChartboostService; 