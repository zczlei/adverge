/**
 * 广告聚合器核心类
 * 实现Header Bidding和Waterfall的混合模型机制
 */

import axios from 'axios';
import BaseAdapter from './adapters/BaseAdapter';

class AdAggregator {
  constructor(config) {
    this.config = config;
    this.maxRetries = 3;
    this.retryDelay = 1000; // 重试延迟时间（毫秒）
    this.adapters = new Map();
    this.performanceMetrics = new Map();
    this.adCache = new Map();
    this.preloadQueue = new Set();
    this.cacheExpiry = 5 * 60 * 1000; // 5分钟缓存过期时间
    this.initializeAdapters();
  }

  async initializeAdapters() {
    // 初始化所有广告平台适配器
    const adapters = [
      new TopOnAdapter(this.config),
      new BigoAdsAdapter(this.config),
      new InMobiAdapter(this.config),
      // ... 其他适配器
    ];

    for (const adapter of adapters) {
      try {
        await adapter.initialize();
        this.adapters.set(adapter.name, adapter);
      } catch (error) {
        console.error(`初始化适配器 ${adapter.name} 失败:`, error);
      }
    }
  }

  async loadAd(containerId, options) {
    const startTime = Date.now();
    this.showLoadingState(containerId);
    let retries = this.maxRetries;
    
    while (retries > 0) {
      try {
        const ad = await this.getAd(options);
        if (ad) {
          const renderStartTime = Date.now();
          await this.renderAd(containerId, ad);
          const renderTime = Date.now() - renderStartTime;
          
          // 记录性能指标
          this.trackAdPerformance(containerId, {
            loadTime: Date.now() - startTime,
            renderTime,
            success: true,
            platform: ad.adapter.name,
            adType: options.adType
          });
          
          return true;
        }
      } catch (error) {
        console.error(`加载广告失败，剩余重试次数: ${retries}`, error);
        retries--;
        if (retries > 0) {
          await this.sleep(this.retryDelay);
        }
      }
    }

    // 记录失败指标
    this.trackAdPerformance(containerId, {
      loadTime: Date.now() - startTime,
      success: false,
      error: '所有重试均失败'
    });
    
    this.showFallbackAd(containerId);
    return false;
  }

  showLoadingState(containerId) {
    const container = document.getElementById(containerId);
    if (container) {
      container.innerHTML = `
        <div class="ad-loading">
          <div class="loading-spinner"></div>
          <div class="loading-text">广告加载中...</div>
          <div class="loading-progress">0%</div>
        </div>
      `;
      this.startLoadingProgress(container);
    }
  }

  startLoadingProgress(container) {
    let progress = 0;
    const progressElement = container.querySelector('.loading-progress');
    const interval = setInterval(() => {
      progress += 5;
      if (progress <= 90) {
        progressElement.textContent = `${progress}%`;
      } else {
        clearInterval(interval);
      }
    }, 100);
  }

  hideLoadingState(containerId) {
    const container = document.getElementById(containerId);
    if (container) {
      const loading = container.querySelector('.ad-loading');
      if (loading) {
        loading.remove();
      }
    }
  }

  async getAd(options) {
    const cacheKey = this.generateCacheKey(options);
    const cachedAd = this.getCachedAd(cacheKey);
    
    if (cachedAd) {
      console.log('使用缓存的广告');
      return cachedAd;
    }

    const startTime = Date.now();
    const bids = await Promise.all(
      Array.from(this.adapters.values()).map(adapter => 
        this.getBidWithTimeout(adapter, options)
      )
    );

    const validBids = bids.filter(bid => bid && bid.cpm > 0);
    if (validBids.length === 0) {
      throw new Error('没有有效的广告竞价');
    }

    // 选择最高出价的广告
    const selectedAd = validBids.reduce((highest, current) => 
      current.cpm > highest.cpm ? current : highest
    );

    // 缓存广告
    this.cacheAd(cacheKey, selectedAd);

    return selectedAd;
  }

  generateCacheKey(options) {
    return `${options.adType}_${options.width}_${options.height}_${this.getUserSegment()}`;
  }

  getUserSegment() {
    // 根据用户特征生成用户分群标识
    const deviceType = this.getDeviceType();
    const os = this.getOS();
    return `${deviceType}_${os}`;
  }

  getCachedAd(cacheKey) {
    const cached = this.adCache.get(cacheKey);
    if (cached && Date.now() - cached.timestamp < this.cacheExpiry) {
      return cached.ad;
    }
    return null;
  }

  cacheAd(cacheKey, ad) {
    this.adCache.set(cacheKey, {
      ad,
      timestamp: Date.now()
    });
  }

  // 预加载广告
  async preloadAds(adTypes) {
    for (const type of adTypes) {
      if (!this.preloadQueue.has(type)) {
        this.preloadQueue.add(type);
        try {
          const options = {
            adType: type,
            width: this.getDefaultWidth(type),
            height: this.getDefaultHeight(type)
          };
          
          const ad = await this.getAd(options);
          if (ad) {
            console.log(`预加载 ${type} 广告成功`);
          }
        } catch (error) {
          console.error(`预加载 ${type} 广告失败:`, error);
        } finally {
          this.preloadQueue.delete(type);
        }
      }
    }
  }

  getDefaultWidth(adType) {
    const defaults = {
      banner: 320,
      interstitial: window.innerWidth,
      native: 300
    };
    return defaults[adType] || 320;
  }

  getDefaultHeight(adType) {
    const defaults = {
      banner: 50,
      interstitial: window.innerHeight,
      native: 250
    };
    return defaults[adType] || 50;
  }

  // 清理过期缓存
  cleanupCache() {
    const now = Date.now();
    for (const [key, value] of this.adCache.entries()) {
      if (now - value.timestamp > this.cacheExpiry) {
        this.adCache.delete(key);
      }
    }
  }

  // 定期清理缓存
  startCacheCleanup() {
    setInterval(() => this.cleanupCache(), this.cacheExpiry);
  }

  // 获取设备类型
  getDeviceType() {
    const ua = navigator.userAgent.toLowerCase();
    if (ua.includes('mobile')) return 'mobile';
    if (ua.includes('tablet')) return 'tablet';
    return 'desktop';
  }

  // 获取操作系统
  getOS() {
    const ua = navigator.userAgent;
    if (ua.includes('Android')) return 'android';
    if (ua.includes('iPhone') || ua.includes('iPad')) return 'ios';
    if (ua.includes('Windows')) return 'windows';
    if (ua.includes('Mac')) return 'macos';
    return 'other';
  }

  async getBidWithTimeout(adapter, options) {
    try {
      const timeoutPromise = new Promise((_, reject) => 
        setTimeout(() => reject(new Error('竞价超时')), this.config.bidTimeout || 1000)
      );

      const bidPromise = adapter.bid(options);
      return await Promise.race([bidPromise, timeoutPromise]);
    } catch (error) {
      console.error(`${adapter.name} 竞价失败:`, error);
      return null;
    }
  }

  async renderAd(containerId, ad) {
    const container = document.getElementById(containerId);
    if (!container) {
      throw new Error(`找不到广告容器: ${containerId}`);
    }

    try {
      this.hideLoadingState(containerId);
      await ad.adapter.render(container, ad);
      
      // 监控广告可见性
      this.monitorAdViewability(containerId, ad);
    } catch (error) {
      throw new Error(`渲染广告失败: ${error.message}`);
    }
  }

  showFallbackAd(containerId) {
    const container = document.getElementById(containerId);
    if (container) {
      this.hideLoadingState(containerId);
      container.innerHTML = '<div class="ad-fallback">暂时没有合适的广告</div>';
    }
  }

  trackAdPerformance(containerId, metrics) {
    const container = document.getElementById(containerId);
    if (!container) return;

    // 更新性能指标
    this.performanceMetrics.set(containerId, {
      ...this.performanceMetrics.get(containerId),
      ...metrics,
      timestamp: Date.now()
    });

    // 发送性能数据到服务器
    this.sendPerformanceMetrics(containerId, metrics);
  }

  async sendPerformanceMetrics(containerId, metrics) {
    try {
      await fetch('/api/metrics/performance', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          containerId,
          ...metrics
        })
      });
    } catch (error) {
      console.error('发送性能指标失败:', error);
    }
  }

  monitorAdViewability(containerId, ad) {
    const container = document.getElementById(containerId);
    if (!container) return;

    let isVisible = false;
    let viewTime = 0;
    let lastVisibleTime = Date.now();

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            if (!isVisible) {
              isVisible = true;
              lastVisibleTime = Date.now();
              this.trackAdImpression(ad);
            }
            viewTime += Date.now() - lastVisibleTime;
            lastVisibleTime = Date.now();
          } else {
            isVisible = false;
          }
        });
      },
      { 
        threshold: 0.5,
        rootMargin: '0px'
      }
    );

    observer.observe(container);

    // 定期报告可见性数据
    setInterval(() => {
      if (isVisible) {
        this.trackAdViewability(containerId, {
          viewTime,
          isVisible,
          timestamp: Date.now()
        });
      }
    }, 5000);
  }

  trackAdViewability(containerId, metrics) {
    this.sendPerformanceMetrics(containerId, {
      type: 'viewability',
      ...metrics
    });
  }

  trackAdImpression(ad) {
    // 记录广告展示
    fetch('/api/metrics/impression', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        adId: ad.id,
        platformId: ad.adapter.name,
        timestamp: Date.now()
      })
    }).catch(error => console.error('记录广告展示失败:', error));
  }

  sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

export default AdAggregator; 