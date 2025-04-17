const BaseAdapter = require('./BaseAdapter');
const axios = require('axios');

/**
 * TopOn广告平台适配器
 * 支持RTB竞价,适合插屏和视频广告
 */
class TopOnAdapter extends BaseAdapter {
  constructor(config) {
    super(config);
    this.rtbEndpoint = config.apiUrl + '/rtb';
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
   * 发起RTB竞价请求
   * @param {string} placementId 广告位ID
   * @returns {Promise<Object>} 竞价结果
   */
  async bid(placementId) {
    if (!this.initialized) {
      await this.initialize();
    }

    if (!this.validatePlacement(placementId)) {
      throw new Error(`无效的广告位ID: ${placementId}`);
    }

    const startTime = Date.now();
    try {
      const placement = this.getPlacementConfig(placementId);
      const bidRequest = this.buildBidRequest(placementId, placement);
      
      const response = await axios.post(this.rtbEndpoint, bidRequest, {
        headers: {
          'Content-Type': 'application/json',
          'X-TopOn-Key': this.config.apiKey
        },
        timeout: 150 // 150ms超时
      });

      if (!response.data.seatbid || response.data.seatbid.length === 0) {
        return null;
      }

      const bid = response.data.seatbid[0].bid[0];
      const latency = Date.now() - startTime;

      // 更新历史eCPM
      this.updateHistoricalEcpm(bid.price);

      return {
        source: 'TopOn',
        price: bid.price,
        creative: {
          html: bid.adm,
          width: bid.w,
          height: bid.h,
          clickUrl: bid.nurl,
          impressionUrl: bid.iurl
        },
        placementId,
        strategy: 'headerBidding',
        latency
      };
    } catch (error) {
      this.logError(error, `竞价失败 [placementId: ${placementId}]`);
      return null;
    }
  }

  /**
   * 构建RTB竞价请求
   * @param {string} placementId 广告位ID
   * @param {Object} placement 广告位配置
   * @returns {Object} OpenRTB请求对象
   */
  buildBidRequest(placementId, placement) {
    return {
      id: this.generateBidId(),
      imp: [{
        id: placementId,
        banner: {
          w: placement.width,
          h: placement.height,
          pos: placement.position
        },
        bidfloor: placement.floorPrice,
        bidfloorcur: 'USD'
      }],
      app: {
        id: this.appId,
        name: this.config.appName,
        bundle: this.config.bundleId,
        domain: window.location.hostname,
        cat: ['NEWS']
      },
      device: this.getDeviceInfo(),
      user: this.getUserInfo(),
      at: 1, // 第一价格竞价
      tmax: 150, // 150ms超时
      cur: ['USD']
    };
  }

  /**
   * 渲染广告
   * @param {Object} creative 广告创意
   * @returns {Promise<HTMLElement>}
   */
  async render(creative) {
    if (!this.validateCreative(creative)) {
      throw new Error('无效的广告创意');
    }

    try {
      const container = this.createAdContainer(creative);
      
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
      iframeDoc.write(creative.html);
      iframeDoc.close();

      // 添加点击跟踪
      if (creative.clickUrl) {
        iframe.contentWindow.addEventListener('click', () => {
          this.trackClick(creative.clickUrl);
        });
      }

      // 添加展示跟踪
      if (creative.impressionUrl) {
        this.addImpressionTracking(container, () => {
          this.trackImpression(creative.impressionUrl);
        });
      }

      return container;
    } catch (error) {
      this.logError(error, '广告渲染失败');
      throw error;
    }
  }

  /**
   * 生成竞价请求ID
   * @returns {string}
   */
  generateBidId() {
    return 'bid_' + Math.random().toString(36).substr(2, 9);
  }

  /**
   * 获取设备信息
   * @returns {Object}
   */
  getDeviceInfo() {
    return {
      ua: navigator.userAgent,
      language: navigator.language,
      w: window.innerWidth,
      h: window.innerHeight,
      js: 1,
      connectiontype: this.getConnectionType(),
      devicetype: this.getDeviceType()
    };
  }

  /**
   * 获取用户信息
   * @returns {Object}
   */
  getUserInfo() {
    return {
      id: this.getUserId(),
      geo: this.getGeoInfo()
    };
  }

  /**
   * 获取连接类型
   * @returns {number}
   */
  getConnectionType() {
    const connection = navigator.connection || navigator.mozConnection || navigator.webkitConnection;
    if (!connection) return 0;
    
    const types = {
      'wifi': 2,
      'cellular': 3,
      '4g': 4,
      '3g': 3,
      '2g': 2,
      'ethernet': 1
    };
    
    return types[connection.type] || 0;
  }

  /**
   * 获取设备类型
   * @returns {number}
   */
  getDeviceType() {
    const ua = navigator.userAgent;
    if (/mobile/i.test(ua)) return 1; // Mobile
    if (/tablet/i.test(ua)) return 2; // Tablet
    return 3; // Desktop
  }

  /**
   * 获取用户ID
   * @returns {string}
   */
  getUserId() {
    let userId = localStorage.getItem('topon_user_id');
    if (!userId) {
      userId = 'user_' + Math.random().toString(36).substr(2, 9);
      localStorage.setItem('topon_user_id', userId);
    }
    return userId;
  }

  /**
   * 获取地理位置信息
   * @returns {Object}
   */
  getGeoInfo() {
    // 这里应该实现实际的地理位置获取逻辑
    return {
      country: 'USA',
      region: 'CA',
      city: 'San Francisco',
      type: 2 // IP-based
    };
  }

  /**
   * 加载外部脚本
   * @param {string} url 脚本URL
   * @returns {Promise<void>}
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
   * @param {string} url 点击跟踪URL
   */
  trackClick(url) {
    const img = new Image();
    img.src = url;
  }

  /**
   * 跟踪展示
   * @param {string} url 展示跟踪URL
   */
  trackImpression(url) {
    const img = new Image();
    img.src = url;
  }
}

module.exports = TopOnAdapter; 