import BaseAdapter from './BaseAdapter';
import axios from 'axios';

class UnityAdsAdapter extends BaseAdapter {
  constructor(config) {
    super(config, 'UnityAds');
    this.apiUrl = config.apiUrl || 'https://api.unity3d.com';
    this.gameId = config.gameId;
    this.placementId = config.placementId;
    this.isInitialized = false;
  }

  async initialize() {
    if (this.isInitialized) return;
    
    try {
      // 加载Unity Ads SDK
      await this.loadScript('https://sdk.unity3d.com/unity-ads-sdk.js');
      
      // 初始化SDK
      window.UnityAds.init({
        gameId: this.gameId,
        placementId: this.placementId,
        debug: this.config.debug || false
      });
      
      this.isInitialized = true;
    } catch (error) {
      this.logError('初始化失败', error);
      throw error;
    }
  }

  async bid(placementId) {
    try {
      await this.initialize();
      
      // 验证广告位配置
      const placement = this.validatePlacement(placementId);
      if (!placement) return null;

      // 构建竞价请求
      const bidRequest = this.buildBidRequest(placement);
      
      // 发送竞价请求
      const response = await axios.post(`${this.apiUrl}/bid`, bidRequest, {
        timeout: 100 // 100ms超时
      });

      if (!response.data || !response.data.creative) {
        return null;
      }

      // 更新历史eCPM
      this.updateHistoricalEcpm(response.data.price);

      return {
        source: 'UnityAds',
        price: response.data.price,
        placementId,
        creative: response.data.creative,
        render: () => this.render(response.data.creative)
      };
    } catch (error) {
      this.logError('竞价请求失败', error);
      return null;
    }
  }

  buildBidRequest(placement) {
    return {
      id: this.generateBidId(),
      imp: [{
        id: placement.id,
        video: {
          w: placement.width,
          h: placement.height,
          minduration: 15,
          maxduration: 30,
          protocols: [2, 3, 5, 6], // VAST 2.0, 3.0, 4.0, 4.1
          mimes: ['video/mp4', 'video/3gpp', 'video/x-flv'],
          linearity: 1, // 线性视频
          skip: 1, // 可跳过
          skipmin: 5, // 5秒后可跳过
          skipafter: 5 // 5秒后显示跳过按钮
        }
      }],
      app: {
        id: this.gameId,
        name: this.config.appName,
        bundle: this.config.bundleId,
        publisher: {
          id: this.placementId
        }
      },
      device: this.getDeviceInfo(),
      user: this.getUserInfo(),
      at: 1, // 1 = 第一价格竞价
      tmax: 100 // 100ms超时
    };
  }

  async render(creative) {
    try {
      // 创建广告容器
      const container = this.createAdContainer(creative.width, creative.height);
      
      // 创建视频播放器
      const video = document.createElement('video');
      video.style.width = '100%';
      video.style.height = '100%';
      video.controls = true;
      video.src = creative.videoUrl;
      
      // 添加跳过按钮
      const skipButton = document.createElement('button');
      skipButton.className = 'skip-button';
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
      skipButton.style.display = 'none'; // 初始隐藏
      
      // 5秒后显示跳过按钮
      setTimeout(() => {
        skipButton.style.display = 'block';
      }, 5000);
      
      // 点击跳过按钮
      skipButton.addEventListener('click', () => {
        video.pause();
        container.style.display = 'none';
      });
      
      // 视频播放完成
      video.addEventListener('ended', () => {
        container.style.display = 'none';
      });
      
      container.appendChild(video);
      container.appendChild(skipButton);
      
      // 添加展示追踪
      this.addImpressionTracking(container, creative.impressionUrl);
      
      // 添加点击追踪
      video.addEventListener('click', () => {
        this.trackClick(creative.clickUrl);
        window.open(creative.clickUrl, '_blank');
      });
      
      return container;
    } catch (error) {
      this.logError('广告渲染失败', error);
      throw error;
    }
  }

  getDeviceInfo() {
    return {
      ua: navigator.userAgent,
      ip: '', // 服务器端填充
      geo: {
        country: this.config.geo?.country || '',
        region: this.config.geo?.region || '',
        city: this.config.geo?.city || ''
      },
      devicetype: this.getDeviceType(),
      os: this.getOS(),
      osv: this.getOSVersion(),
      h: screen.height,
      w: screen.width,
      language: navigator.language,
      carrier: this.getCarrier(),
      connectiontype: this.getConnectionType()
    };
  }

  getUserInfo() {
    return {
      id: this.config.userId || '',
      yob: this.config.userAge ? new Date().getFullYear() - this.config.userAge : '',
      gender: this.config.userGender || '',
      keywords: this.config.userKeywords || [],
      interests: this.config.userInterests || []
    };
  }

  getDeviceType() {
    const ua = navigator.userAgent.toLowerCase();
    if (ua.includes('mobile')) return 1;
    if (ua.includes('tablet')) return 2;
    return 0; // 桌面设备
  }

  getOS() {
    const ua = navigator.userAgent;
    if (ua.includes('Android')) return 'android';
    if (ua.includes('iPhone') || ua.includes('iPad')) return 'ios';
    if (ua.includes('Windows')) return 'windows';
    if (ua.includes('Mac')) return 'macos';
    return 'other';
  }

  getOSVersion() {
    const ua = navigator.userAgent;
    const match = ua.match(/(Android|iPhone|iPad|Windows|Mac OS X)\s+([\d._]+)/);
    return match ? match[2] : '';
  }

  getCarrier() {
    // 在移动设备上可以通过其他方式获取运营商信息
    return '';
  }

  getConnectionType() {
    const connection = navigator.connection || navigator.mozConnection || navigator.webkitConnection;
    if (connection) {
      return connection.type || connection.effectiveType || '';
    }
    return '';
  }

  generateBidId() {
    return `unityads_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  async loadScript(url) {
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = url;
      script.async = true;
      script.onload = resolve;
      script.onerror = reject;
      document.head.appendChild(script);
    });
  }

  async trackClick(clickUrl) {
    try {
      await axios.get(clickUrl);
    } catch (error) {
      this.logError('点击上报失败', error);
    }
  }
}

export default UnityAdsAdapter; 