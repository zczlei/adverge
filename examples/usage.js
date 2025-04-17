const AdAggregator = require('../src/sdk/AdAggregator');
const { adConfig, placementTypes } = require('../src/config/adConfig');

// 初始化广告聚合器
const aggregator = new AdAggregator(adConfig);

// 示例1: 使用Header Bidding加载文章顶部横幅广告
async function loadTopBanner() {
  try {
    const adElement = await aggregator.requestAd(
      placementTypes.BANNER_TOP,
      'headerBidding'
    );
    document.getElementById('top-banner-container').appendChild(adElement);
    console.log('顶部横幅广告加载成功');
  } catch (error) {
    console.error('顶部横幅广告加载失败:', error);
  }
}

// 示例2: 使用Waterfall加载文章列表原生广告
async function loadNativeFeed() {
  try {
    const adElement = await aggregator.requestAd(
      placementTypes.NATIVE_FEED,
      'waterfall'
    );
    document.getElementById('native-feed-container').appendChild(adElement);
    console.log('原生广告加载成功');
  } catch (error) {
    console.error('原生广告加载失败:', error);
  }
}

// 示例3: 页面切换时加载插屏广告
let lastInterstitialTime = 0;
const INTERSTITIAL_INTERVAL = 5 * 60 * 1000; // 5分钟间隔

async function loadInterstitial() {
  const now = Date.now();
  if (now - lastInterstitialTime < INTERSTITIAL_INTERVAL) {
    console.log('插屏广告展示间隔未到');
    return;
  }

  try {
    const adElement = await aggregator.requestAd(
      placementTypes.INTERSTITIAL,
      'headerBidding'
    );
    
    // 创建模态框容器
    const modal = document.createElement('div');
    modal.style.position = 'fixed';
    modal.style.top = '0';
    modal.style.left = '0';
    modal.style.width = '100%';
    modal.style.height = '100%';
    modal.style.backgroundColor = 'rgba(0,0,0,0.7)';
    modal.style.display = 'flex';
    modal.style.justifyContent = 'center';
    modal.style.alignItems = 'center';
    modal.style.zIndex = '9999';

    // 添加关闭按钮
    const closeButton = document.createElement('button');
    closeButton.textContent = '关闭';
    closeButton.style.position = 'absolute';
    closeButton.style.top = '10px';
    closeButton.style.right = '10px';
    closeButton.style.padding = '5px 10px';
    closeButton.style.cursor = 'pointer';
    closeButton.onclick = () => {
      document.body.removeChild(modal);
    };

    modal.appendChild(closeButton);
    modal.appendChild(adElement);
    document.body.appendChild(modal);

    lastInterstitialTime = now;
    console.log('插屏广告加载成功');
  } catch (error) {
    console.error('插屏广告加载失败:', error);
  }
}

// 示例4: 监听页面可见性变化,控制广告加载
document.addEventListener('visibilitychange', () => {
  if (document.visibilityState === 'visible') {
    // 页面变为可见时重新加载广告
    loadTopBanner();
    loadNativeFeed();
  }
});

// 示例5: 页面滚动时懒加载广告
let isLoadingAd = false;
window.addEventListener('scroll', () => {
  if (isLoadingAd) return;

  const feedContainer = document.getElementById('native-feed-container');
  const rect = feedContainer.getBoundingClientRect();
  
  if (rect.top < window.innerHeight && rect.bottom >= 0) {
    isLoadingAd = true;
    loadNativeFeed().finally(() => {
      isLoadingAd = false;
    });
  }
});

// 示例6: 页面切换时加载插屏
window.addEventListener('popstate', () => {
  loadInterstitial();
});

// 初始加载
document.addEventListener('DOMContentLoaded', () => {
  loadTopBanner();
  loadNativeFeed();
}); 