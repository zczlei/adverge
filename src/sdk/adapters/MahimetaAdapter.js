import BaseAdapter from './BaseAdapter';
import axios from 'axios';

class MahimetaAdapter extends BaseAdapter {
  constructor(config) {
    super(config, 'Mahimeta');
    this.apiUrl = config.apiUrl || 'https://api.mahimeta.com';
    this.appKey = config.appKey;
    this.userId = config.userId;
    this.isInitialized = false;
  }

  async initialize() {
    if (this.isInitialized) return;
    
    try {
      // 加载Mahimeta SDK
      await this.loadScript('https://sdk.mahimeta.com/mahimeta-ads-sdk.js');
      
      // 初始化SDK
      window.Mahimeta.init({
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
        source: 'Mahimeta',
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
        native: {
          request: {
            assets: [
              {
                id: 1,
                required: 1,
                title: {
                  len: 100
                }
              },
              {
                id: 2,
                required: 1,
                img: {
                  type: 1, // 主图
                  w: placement.width,
                  h: placement.height
                }
              },
              {
                id: 3,
                required: 0,
                data: {
                  type: 1, // 描述
                  len: 200
                }
              },
              {
                id: 4,
                required: 0,
                data: {
                  type: 2, // 评分
                  len: 10
                }
              }
            ],
            ver: '1.2'
          }
        }
      }],
      app: {
        id: this.appKey,
        name: this.config.appName,
        bundle: this.config.bundleId,
        publisher: {
          id: this.userId
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
      
      // 创建原生广告内容
      const adContent = document.createElement('div');
      adContent.className = 'mahimeta-native-ad';
      adContent.style.cssText = `
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        background: #fff;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      `;
      
      // 添加标题
      const title = document.createElement('h3');
      title.textContent = creative.title;
      title.style.cssText = `
        margin: 0;
        padding: 12px;
        font-size: 16px;
        font-weight: bold;
        color: #333;
      `;
      
      // 添加图片
      const img = document.createElement('img');
      img.src = creative.imageUrl;
      img.style.cssText = `
        width: 100%;
        height: auto;
        object-fit: cover;
      `;
      
      // 添加描述
      const desc = document.createElement('p');
      desc.textContent = creative.description;
      desc.style.cssText = `
        margin: 0;
        padding: 12px;
        font-size: 14px;
        color: #666;
      `;
      
      // 添加评分
      if (creative.rating) {
        const rating = document.createElement('div');
        rating.className = 'rating';
        rating.style.cssText = `
          padding: 12px;
          display: flex;
          align-items: center;
        `;
        
        const stars = document.createElement('div');
        stars.style.cssText = `
          color: #ffc107;
          margin-right: 8px;
        `;
        stars.textContent = '★★★★★'.slice(0, creative.rating);
        
        const ratingText = document.createElement('span');
        ratingText.textContent = `${creative.rating}/5`;
        ratingText.style.cssText = `
          font-size: 14px;
          color: #666;
        `;
        
        rating.appendChild(stars);
        rating.appendChild(ratingText);
        adContent.appendChild(rating);
      }
      
      adContent.appendChild(title);
      adContent.appendChild(img);
      adContent.appendChild(desc);
      
      container.appendChild(adContent);
      
      // 添加点击追踪
      container.addEventListener('click', () => {
        this.trackClick(creative.clickUrl);
        window.open(creative.clickUrl, '_blank');
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
    return `mahimeta_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
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

export default MahimetaAdapter; 