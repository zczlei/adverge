import BaseAdapter from './BaseAdapter';
import axios from 'axios';

class FyberAdapter extends BaseAdapter {
  constructor(config) {
    super(config, 'Fyber');
    this.apiUrl = config.apiUrl || 'https://api.fyber.com';
    this.appKey = config.appKey;
    this.appId = config.appId;
    this.userId = config.userId;
    this.isInitialized = false;
  }

  async initialize() {
    if (this.isInitialized) return;
    
    try {
      // 加载Fyber SDK
      await this.loadScript('https://sdk.fyber.com/fyber-ads-sdk.js');
      
      // 初始化SDK
      window.Fyber.init({
        appId: this.appId,
        appKey: this.appKey,
        userId: this.userId,
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
        source: 'Fyber',
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
          pos: placement.position,
          api: [3, 5], // MRAID 2.0, 3.0
          btype: [1, 2, 3, 4], // 静态图片, GIF, HTML, 视频
          battr: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15] // 所有创意属性
        }
      }],
      app: {
        id: this.appId,
        name: this.config.appName,
        bundle: this.config.bundleId,
        publisher: {
          id: this.appKey
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
            <script src="https://cdn.fyber.com/mraid.js"></script>
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
      language: navigator.language,
      carrier: this.getCarrier(),
      connectiontype: this.getConnectionType()
    };
  }

  getUserInfo() {
    return {
      id: this.userId,
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
    return `fyber_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
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

export default FyberAdapter; 