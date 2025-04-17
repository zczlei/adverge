/**
 * 广告聚合器核心类
 * 实现广告填充和展示流程
 */

import axios from 'axios';
import BaseAdapter from './adapters/BaseAdapter';

class AdAggregator {
  constructor(config) {
    this.config = config;
    this.adapters = new Map();
    this.initialized = false;
  }

  async initialize() {
    try {
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

      this.initialized = true;
      console.log('广告聚合器初始化成功');
    } catch (error) {
      console.error('广告聚合器初始化失败:', error);
      throw error;
    }
  }

  // 加载广告
  async loadAd(containerId, options) {
    if (!this.initialized) {
      throw new Error('广告聚合器未初始化');
    }

    try {
      // 1. 向 Adverge 服务器发起广告填充请求
      const { winningAN, bidToken } = await this.requestAdFill(options.adUnitId, options);
      
      if (!winningAN || !bidToken) {
        throw new Error('未获取到胜出 AN 信息');
      }

      // 2. 获取胜出 AN 的适配器
      const adapter = this.adapters.get(winningAN);
      if (!adapter) {
        throw new Error(`未找到 AN 适配器: ${winningAN}`);
      }

      // 3. 调用胜出 AN 的广告加载接口
      const ad = await adapter.loadAd(options.adUnitId, bidToken);
      
      if (!ad) {
        throw new Error('广告加载失败');
      }

      // 4. 展示广告
      await this.showAd(containerId, ad, winningAN);

      return true;
    } catch (error) {
      console.error('广告加载失败:', error);
      this.showErrorState(containerId);
      return false;
    }
  }

  // 发起广告填充请求
  async requestAdFill(adUnitId, options) {
    try {
      const response = await axios.post(`${this.config.apiUrl}/ad/fill`, {
        adUnitId,
        ...options
      });

      if (!response.data || !response.data.winningAN) {
        throw new Error('广告填充请求响应无效');
      }

      return response.data;
    } catch (error) {
      console.error('广告填充请求失败:', error);
      throw error;
    }
  }

  // 展示广告
  async showAd(containerId, ad, winningAN) {
    const container = document.getElementById(containerId);
    if (!container) {
      throw new Error(`未找到广告容器: ${containerId}`);
    }

    try {
      // 获取胜出 AN 的适配器
      const adapter = this.adapters.get(winningAN);
      if (!adapter) {
        throw new Error(`未找到 AN 适配器: ${winningAN}`);
      }

      // 调用适配器的展示方法
      await adapter.showAd(container, ad);

      // 记录广告展示
      await this.trackImpression(ad.id, winningAN);
    } catch (error) {
      console.error('广告展示失败:', error);
      throw error;
    }
  }

  // 显示错误状态
  showErrorState(containerId) {
    const container = document.getElementById(containerId);
    if (container) {
      container.innerHTML = `
        <div class="ad-error">
          <div class="error-icon">!</div>
          <div class="error-text">广告加载失败</div>
          <button class="retry-button">重试</button>
        </div>
      `;

      // 添加重试按钮事件
      const retryButton = container.querySelector('.retry-button');
      if (retryButton) {
        retryButton.addEventListener('click', () => {
          this.loadAd(containerId, this.lastOptions);
        });
      }
    }
  }

  // 记录广告展示
  async trackImpression(adId, platform) {
    try {
      await axios.post(`${this.config.apiUrl}/track/impression`, {
        adId,
        platform
      });
    } catch (error) {
      console.error('记录广告展示失败:', error);
    }
  }

  // 记录广告点击
  async trackClick(adId, platform, revenue) {
    try {
      await axios.post(`${this.config.apiUrl}/track/click`, {
        adId,
        platform,
        revenue
      });
    } catch (error) {
      console.error('记录广告点击失败:', error);
    }
  }
}

export default AdAggregator; 