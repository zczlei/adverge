import BaseAdapter from './BaseAdapter';
import axios from 'axios';

class BigoAdsAdapter extends BaseAdapter {
  constructor(config) {
    super(config, 'BigoAds');
    this.apiUrl = config.apiUrl || 'https://api.bigo.ads';
    this.appKey = config.appKey;
    this.isInitialized = false;
  }

  async initialize() {
    if (this.isInitialized) return;
    
    try {
      // 加载Bigo Ads SDK
      await this.loadScript('https://sdk.bigo.ads/bigo-ads-sdk.js');
      
      // 初始化SDK
      window.BigoAds.init({
        appKey: this.appKey,
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
        source: 'BigoAds',
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
        banner: {
          w: placement.width,
          h: placement.height,
          pos: placement.position
        }
      }],
      app: {
        id: this.appKey,
        name: this.config.appName,
        bundle: this.config.bundleId
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
      
      // 创建iframe以隔离广告内容
      const iframe = document.createElement('iframe');
      iframe.style.border = 'none';
      iframe.style.width = '100%';
      iframe.style.height = '100%';
      
      // 写入广告内容
      iframe.srcdoc = `
        <!DOCTYPE html>
        <html>
          <head>
            <style>
              body { margin: 0; padding: 0; }
              img { max-width: 100%; height: auto; }
            </style>
          </head>
          <body>
            ${creative.html}
          </body>
        </html>
      `;
      
      container.appendChild(iframe);
      
      // 添加点击追踪
      iframe.addEventListener('load', () => {
        this.trackClick(iframe.contentWindow, creative.clickUrl);
      });
      
      // 添加展示追踪
      this.addImpressionTracking(container, creative.impressionUrl);
      
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
      language: navigator.language
    };
  }

  getUserInfo() {
    return {
      id: this.config.userId || '',
      yob: this.config.userAge ? new Date().getFullYear() - this.config.userAge : '',
      gender: this.config.userGender || '',
      keywords: this.config.userKeywords || []
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

  generateBidId() {
    return `bigo_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
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

  trackClick(win, clickUrl) {
    win.addEventListener('click', (e) => {
      e.preventDefault();
      this.reportClick(clickUrl);
      window.open(clickUrl, '_blank');
    });
  }

  async reportClick(clickUrl) {
    try {
      await axios.get(clickUrl);
    } catch (error) {
      this.logError('点击上报失败', error);
    }
  }
}

export default BigoAdsAdapter; 