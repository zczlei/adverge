import BaseAdapter from './BaseAdapter';
import axios from 'axios';

class VungleAdapter extends BaseAdapter {
  constructor(config) {
    super(config, 'Vungle');
    this.apiUrl = config.apiUrl || 'https://api.vungle.com';
    this.appId = config.appId;
    this.isInitialized = false;
  }

  async initialize() {
    if (this.isInitialized) return;
    
    try {
      // 加载Vungle SDK
      await this.loadScript('https://sdk.vungle.com/vungle-ads-sdk.js');
      
      // 初始化SDK
      window.Vungle.init({
        appId: this.appId,
        debug: this.config.debug || false
      });
      
      this.isInitialized = true;
    } catch (error) {
      this.logError('初始化失败', error);
      throw error;
    }
  }

  async loadAd(adData) {
    try {
      await this.initialize();
      
      // 调用Vungle SDK加载广告
      const ad = await window.Vungle.loadAd(adData.placementId);
      
      if (!ad) {
        throw new Error('广告加载失败');
      }

      return {
        id: ad.id,
        creative: {
          html: ad.html,
          width: ad.width,
          height: ad.height,
          clickUrl: ad.clickUrl,
          impressionUrl: ad.impressionUrl,
          title: ad.title,
          description: ad.description,
          mainImage: ad.mainImage,
          icon: ad.icon
        }
      };
    } catch (error) {
      this.logError('广告加载失败', error);
      return null;
    }
  }

  async showAd(container, ad) {
    try {
      // 创建广告容器
      const adContainer = this.createAdContainer(ad.creative.width, ad.creative.height);
      
      // 创建原生广告元素
      const adElement = document.createElement('div');
      adElement.className = 'vungle-native-ad';
      
      // 添加标题
      if (ad.creative.title) {
        const title = document.createElement('h3');
        title.textContent = ad.creative.title;
        adElement.appendChild(title);
      }
      
      // 添加主图
      if (ad.creative.mainImage) {
        const mainImage = document.createElement('img');
        mainImage.src = ad.creative.mainImage;
        mainImage.style.width = '100%';
        mainImage.style.height = 'auto';
        adElement.appendChild(mainImage);
      }
      
      // 添加描述
      if (ad.creative.description) {
        const description = document.createElement('p');
        description.textContent = ad.creative.description;
        adElement.appendChild(description);
      }
      
      // 添加图标
      if (ad.creative.icon) {
        const icon = document.createElement('img');
        icon.src = ad.creative.icon;
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
      if (ad.creative.clickUrl) {
        adElement.addEventListener('click', () => {
          this.trackClick(ad.creative.clickUrl);
          window.open(ad.creative.clickUrl, '_blank');
        });
      }
      
      container.appendChild(adElement);
      
      // 添加展示追踪
      if (ad.creative.impressionUrl) {
        this.addImpressionTracking(container, ad.creative.impressionUrl);
      }
    } catch (error) {
      this.logError('广告渲染失败', error);
      throw error;
    }
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

  trackClick(clickUrl) {
    if (clickUrl) {
      const img = new Image();
      img.src = clickUrl;
    }
  }

  trackImpression(impressionUrl) {
    if (impressionUrl) {
      const img = new Image();
      img.src = impressionUrl;
    }
  }
}

export default VungleAdapter; 