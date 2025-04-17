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

  async loadAd(adData) {
    try {
      await this.initialize();
      
      // 调用Bigo Ads SDK加载广告
      const ad = await window.BigoAds.loadAd(adData.placementId);
      
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
          impressionUrl: ad.impressionUrl
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
            ${ad.creative.html}
          </body>
        </html>
      `;
      
      container.appendChild(iframe);
      
      // 添加点击追踪
      if (ad.creative.clickUrl) {
        iframe.addEventListener('load', () => {
          this.trackClick(iframe.contentWindow, ad.creative.clickUrl);
        });
      }
      
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

  trackClick(win, clickUrl) {
    if (clickUrl) {
      win.addEventListener('click', () => {
        const img = new Image();
        img.src = clickUrl;
      });
    }
  }

  trackImpression(impressionUrl) {
    if (impressionUrl) {
      const img = new Image();
      img.src = impressionUrl;
    }
  }
}

export default BigoAdsAdapter; 