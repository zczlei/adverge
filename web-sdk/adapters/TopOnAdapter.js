const BaseAdapter = require('./BaseAdapter');
const axios = require('axios');

/**
 * TopOn广告平台适配器
 */
class TopOnAdapter extends BaseAdapter {
  constructor(config) {
    super(config);
    this.apiUrl = config.apiUrl;
    this.appId = config.appId;
    this.initialized = false;
  }

  /**
   * 初始化TopOn SDK
   */
  async initialize() {
    if (this.initialized) return;

    try {
      // 加载TopOn SDK
      await this.loadScript('https://sdk.topon.com/js/sdk.js');
      
      // 初始化SDK
      window.TopOn.initialize({
        appId: this.appId,
        debug: this.config.debug || false
      });

      this.initialized = true;
      console.log('[TopOn] SDK初始化成功');
    } catch (error) {
      this.logError(error, '初始化失败');
      throw error;
    }
  }

  /**
   * 加载广告
   * @param {Object} adData 广告数据
   * @returns {Promise<Object>} 广告对象
   */
  async loadAd(adData) {
    if (!this.initialized) {
      await this.initialize();
    }

    try {
      // 调用TopOn SDK加载广告
      const ad = await window.TopOn.loadAd(adData.placementId);
      
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
      this.logError(error, '广告加载失败');
      return null;
    }
  }

  /**
   * 渲染广告
   * @param {HTMLElement} container 广告容器
   * @param {Object} ad 广告对象
   */
  async showAd(container, ad) {
    if (!this.validateCreative(ad.creative)) {
      throw new Error('无效的广告创意');
    }

    try {
      // 创建广告容器
      const adContainer = this.createAdContainer(ad.creative);
      
      // 创建iframe隔离广告内容
      const iframe = document.createElement('iframe');
      this.applyStyles(iframe, {
        width: '100%',
        height: '100%',
        border: 'none'
      });
      
      container.appendChild(iframe);

      // 写入广告内容
      const iframeDoc = iframe.contentWindow.document;
      iframeDoc.open();
      iframeDoc.write(ad.creative.html);
      iframeDoc.close();

      // 添加点击跟踪
      if (ad.creative.clickUrl) {
        iframe.contentWindow.addEventListener('click', () => {
          this.trackClick(ad.creative.clickUrl);
        });
      }

      // 添加展示跟踪
      if (ad.creative.impressionUrl) {
        this.addImpressionTracking(container, () => {
          this.trackImpression(ad.creative.impressionUrl);
        });
      }
    } catch (error) {
      this.logError(error, '广告渲染失败');
      throw error;
    }
  }

  /**
   * 加载脚本
   * @param {string} url 脚本URL
   * @returns {Promise}
   */
  loadScript(url) {
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = url;
      script.async = true;
      script.onload = resolve;
      script.onerror = reject;
      document.head.appendChild(script);
    });
  }

  /**
   * 跟踪点击
   * @param {string} url 点击URL
   */
  trackClick(url) {
    if (url) {
      const img = new Image();
      img.src = url;
    }
  }

  /**
   * 跟踪展示
   * @param {string} url 展示URL
   */
  trackImpression(url) {
    if (url) {
      const img = new Image();
      img.src = url;
    }
  }
}

module.exports = TopOnAdapter; 