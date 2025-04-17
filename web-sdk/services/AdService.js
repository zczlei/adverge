import axios from 'axios';

class AdService {
  constructor() {
    this.baseUrl = '/api';
  }

  async requestAd(adType, platform, options = {}) {
    try {
      const response = await axios.get(`${this.baseUrl}/ad`, {
        params: {
          adType,
          platform,
          ...options
        }
      });

      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || '获取广告失败');
      }
    } catch (error) {
      console.error('广告请求失败:', error);
      throw error;
    }
  }

  async trackImpression(platform, bidId) {
    try {
      await axios.get(`${this.baseUrl}/track/impression`, {
        params: { platform, bidId }
      });
    } catch (error) {
      console.error('展示跟踪失败:', error);
    }
  }

  async trackClick(platform, bidId, adType, deviceType) {
    try {
      await axios.get(`${this.baseUrl}/track/click`, {
        params: { platform, bidId, adType, deviceType }
      });
    } catch (error) {
      console.error('点击跟踪失败:', error);
    }
  }

  async renderAd(container, adData) {
    const { creative, tracking } = adData;

    // 创建广告容器
    const adContainer = document.createElement('div');
    adContainer.className = 'ad-container';
    container.appendChild(adContainer);

    // 渲染广告内容
    if (creative.type === 'html') {
      adContainer.innerHTML = creative.content;
    } else if (creative.type === 'image') {
      const img = document.createElement('img');
      img.src = creative.url;
      img.alt = creative.alt || '';
      adContainer.appendChild(img);
    } else if (creative.type === 'video') {
      const video = document.createElement('video');
      video.src = creative.url;
      video.controls = true;
      video.autoplay = true;
      adContainer.appendChild(video);
    }

    // 添加点击事件
    if (creative.clickUrl) {
      adContainer.addEventListener('click', () => {
        this.trackClick(
          adData.platform,
          adData.bidId,
          adData.adType,
          this.getDeviceType()
        );
        window.open(creative.clickUrl, '_blank');
      });
    }

    // 跟踪展示
    this.trackImpression(adData.platform, adData.bidId);

    return adContainer;
  }

  getDeviceType() {
    const userAgent = navigator.userAgent;
    if (/mobile/i.test(userAgent)) {
      return 'mobile';
    } else if (/tablet/i.test(userAgent)) {
      return 'tablet';
    } else {
      return 'desktop';
    }
  }
}

export default new AdService(); 