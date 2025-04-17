/**
 * 广告平台适配器基类
 * 定义所有广告平台适配器必须实现的接口
 */

class BaseAdapter {
  constructor(config) {
    this.config = config;
    this.historicalEcpm = 0;
    this.name = this.constructor.name;
  }

  /**
   * 初始化适配器
   * @returns {Promise<void>}
   */
  async initialize() {
    throw new Error('必须实现initialize方法');
  }

  /**
   * 发起广告竞价
   * @param {string} placementId 广告位ID
   * @returns {Promise<Object>} 竞价结果
   */
  async bid(placementId) {
    throw new Error('必须实现bid方法');
  }

  /**
   * 渲染广告
   * @param {Object} creative 广告创意
   * @returns {Promise<HTMLElement>} 广告DOM元素
   */
  async render(creative) {
    throw new Error('必须实现render方法');
  }

  /**
   * 更新历史eCPM
   * @param {number} ecpm 新的eCPM值
   */
  updateHistoricalEcpm(ecpm) {
    // 使用移动平均值更新历史eCPM
    const alpha = 0.2; // 平滑因子
    this.historicalEcpm = alpha * ecpm + (1 - alpha) * this.historicalEcpm;
  }

  /**
   * 检查广告位配置是否有效
   * @param {string} placementId 广告位ID
   * @returns {boolean}
   */
  validatePlacement(placementId) {
    return !!this.config.placements[placementId];
  }

  /**
   * 获取广告位配置
   * @param {string} placementId 广告位ID
   * @returns {Object}
   */
  getPlacementConfig(placementId) {
    return this.config.placements[placementId];
  }

  /**
   * 记录错误
   * @param {Error} error 错误对象
   * @param {string} context 错误上下文
   */
  logError(error, context) {
    console.error(`[${this.name}] ${context}:`, error);
    // TODO: 发送错误到监控系统
  }

  /**
   * 检查广告创意是否符合要求
   * @param {Object} creative 广告创意
   * @returns {boolean}
   */
  validateCreative(creative) {
    return !!(creative && creative.html && creative.width && creative.height);
  }

  /**
   * 创建广告容器
   * @param {Object} creative 广告创意
   * @returns {HTMLElement}
   */
  createAdContainer(creative) {
    const container = document.createElement('div');
    container.className = 'ad-container';
    container.style.width = `${creative.width}px`;
    container.style.height = `${creative.height}px`;
    container.style.overflow = 'hidden';
    container.style.position = 'relative';
    return container;
  }

  /**
   * 应用广告样式
   * @param {HTMLElement} element 广告元素
   * @param {Object} styles 样式对象
   */
  applyStyles(element, styles) {
    Object.assign(element.style, styles);
  }

  /**
   * 检查广告是否可见
   * @param {HTMLElement} element 广告元素
   * @returns {boolean}
   */
  isVisible(element) {
    const rect = element.getBoundingClientRect();
    return (
      rect.top >= 0 &&
      rect.left >= 0 &&
      rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
      rect.right <= (window.innerWidth || document.documentElement.clientWidth)
    );
  }

  /**
   * 添加展示追踪
   * @param {HTMLElement} element 广告元素
   * @param {Function} callback 回调函数
   */
  addImpressionTracking(element, callback) {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            callback();
            observer.disconnect();
          }
        });
      },
      { threshold: 0.5 }
    );
    observer.observe(element);
  }
}

module.exports = BaseAdapter; 