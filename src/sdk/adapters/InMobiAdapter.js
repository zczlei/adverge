import BaseAdapter from './BaseAdapter';
import axios from 'axios';

class InMobiAdapter extends BaseAdapter {
  constructor(config) {
    super(config, 'InMobi');
    this.apiUrl = config.apiUrl || 'https://api.inmobi.com';
    this.appKey = config.appKey;
    this.accountId = config.accountId;
    this.isInitialized = false;
  }

  async initialize() {
    if (this.isInitialized) return;
    
    try {
      // 加载InMobi SDK
      await this.loadScript('https://sdk.inmobi.com/inmobi-ads-sdk.js');
      
      // 初始化SDK
      window.InMobi.init({
        accountId: this.accountId,
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
        source: 'InMobi',
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
              { id: 1, required: 1, title: { len: 50 } },
              { id: 2, required: 1, img: { type: 1, w: placement.width, h: placement.height } },
              { id: 3, required: 1, data: { type: 2, len: 100 } },
              { id: 4, required: 0, img: { type: 3, w: 80, h: 80 } }
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
          id: this.accountId
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
      
      // 创建原生广告元素
      const adElement = document.createElement('div');
      adElement.className = 'inmobi-native-ad';
      
      // 添加标题
      const title = document.createElement('h3');
      title.textContent = creative.title;
      adElement.appendChild(title);
      
      // 添加主图
      const mainImage = document.createElement('img');
      mainImage.src = creative.mainImage;
      mainImage.style.width = '100%';
      mainImage.style.height = 'auto';
      adElement.appendChild(mainImage);
      
      // 添加描述
      const description = document.createElement('p');
      description.textContent = creative.description;
      adElement.appendChild(description);
      
      // 添加图标
      if (creative.icon) {
        const icon = document.createElement('img');
        icon.src = creative.icon;
        icon.style.width = '40px';
        icon.style.height = '40px';
        icon.style.borderRadius = '50%';
        adElement.appendChild(icon);
      }
      
      // 添加赞助商标签
      const sponsored = document.createElement('div');
      sponsored.className = 'sponsored-label';
      sponsored.textContent = 'Sponsored';
      adElement.appendChild(sponsored);
      
      // 添加点击事件
      adElement.addEventListener('click', () => {
        this.trackClick(creative.clickUrl);
        window.open(creative.clickUrl, '_blank');
      });
      
      container.appendChild(adElement);
      
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
      carrier: this.getCarrier()
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

  generateBidId() {
    return `inmobi_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
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

export default InMobiAdapter; 