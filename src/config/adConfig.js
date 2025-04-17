/**
 * 广告平台配置
 */
const adConfig = {
  // TopOn配置
  topon: {
    appId: 'your-topon-app-id',
    apiKey: 'your-topon-api-key',
    apiUrl: 'https://api.topon.com/v1',
    appName: 'Your News App',
    bundleId: 'com.yournewsapp',
    debug: false,
    placements: {
      // 文章内容页面顶部横幅
      'banner-top': {
        width: 728,
        height: 90,
        position: 1, // 页面顶部
        floorPrice: 0.5 // 最低eCPM
      },
      // 文章内容页面底部横幅
      'banner-bottom': {
        width: 728,
        height: 90,
        position: 2, // 页面底部
        floorPrice: 0.3
      },
      // 文章列表原生广告
      'native-feed': {
        width: 300,
        height: 250,
        position: 3, // 信息流
        floorPrice: 1.0
      },
      // 页面切换插屏
      'interstitial': {
        width: 320,
        height: 480,
        position: 4, // 全屏
        floorPrice: 2.0
      }
    }
  },

  // InMobi配置
  inmobi: {
    appKey: 'your-inmobi-key',
    apiUrl: 'https://api.inmobi.com/v1',
    placements: {
      'banner-top': {
        width: 728,
        height: 90,
        position: 1,
        floorPrice: 0.6
      },
      'native-feed': {
        width: 300,
        height: 250,
        position: 3,
        floorPrice: 1.2
      }
    }
  },

  // Mintegral配置
  mintegral: {
    appKey: 'your-mintegral-key',
    apiUrl: 'https://api.mintegral.com/v1',
    placements: {
      'native-feed': {
        width: 300,
        height: 250,
        position: 3,
        floorPrice: 1.1
      },
      'interstitial': {
        width: 320,
        height: 480,
        position: 4,
        floorPrice: 2.2
      }
    }
  },

  // ironSource配置
  ironSource: {
    appKey: 'your-ironsource-key',
    apiUrl: 'https://api.ironsource.com/v1',
    placements: {
      'banner-top': {
        width: 728,
        height: 90,
        position: 1,
        floorPrice: 0.4
      },
      'banner-bottom': {
        width: 728,
        height: 90,
        position: 2,
        floorPrice: 0.3
      },
      'interstitial': {
        width: 320,
        height: 480,
        position: 4,
        floorPrice: 1.8
      }
    }
  },

  // Unity Ads配置
  unityAds: {
    gameId: 'your-unity-game-id',
    apiUrl: 'https://api.unityads.com/v1',
    placements: {
      'interstitial': {
        width: 320,
        height: 480,
        position: 4,
        floorPrice: 1.5
      }
    }
  },

  // Vungle配置
  vungle: {
    appId: 'your-vungle-app-id',
    apiUrl: 'https://api.vungle.com/v1',
    placements: {
      'interstitial': {
        width: 320,
        height: 480,
        position: 4,
        floorPrice: 1.6
      }
    }
  },

  // Fyber配置
  fyber: {
    appKey: 'your-fyber-key',
    apiUrl: 'https://api.fyber.com/v1',
    placements: {
      'banner-top': {
        width: 728,
        height: 90,
        position: 1,
        floorPrice: 0.5
      },
      'native-feed': {
        width: 300,
        height: 250,
        position: 3,
        floorPrice: 0.9
      }
    }
  },

  // Chartboost配置
  chartboost: {
    appId: 'your-chartboost-app-id',
    apiUrl: 'https://api.chartboost.com/v1',
    placements: {
      'interstitial': {
        width: 320,
        height: 480,
        position: 4,
        floorPrice: 1.7
      }
    }
  },

  // Bigo Ads配置
  bigoAds: {
    appKey: 'your-bigo-key',
    apiUrl: 'https://api.bigo.tv/ads/v1',
    placements: {
      'banner-top': {
        width: 728,
        height: 90,
        position: 1,
        floorPrice: 0.4
      },
      'banner-bottom': {
        width: 728,
        height: 90,
        position: 2,
        floorPrice: 0.3
      }
    }
  },

  // Mahimeta配置
  mahimeta: {
    appKey: 'your-mahimeta-key',
    apiUrl: 'https://api.mahimeta.com/v1',
    placements: {
      'banner-top': {
        width: 728,
        height: 90,
        position: 1,
        floorPrice: 0.3
      },
      'native-feed': {
        width: 300,
        height: 250,
        position: 3,
        floorPrice: 0.8
      }
    }
  }
};

// 广告位类型定义
const placementTypes = {
  BANNER_TOP: 'banner-top',
  BANNER_BOTTOM: 'banner-bottom',
  NATIVE_FEED: 'native-feed',
  INTERSTITIAL: 'interstitial'
};

// 广告位位置定义
const placementPositions = {
  TOP: 1,
  BOTTOM: 2,
  FEED: 3,
  FULLSCREEN: 4
};

// 广告尺寸定义
const adSizes = {
  BANNER: {
    width: 728,
    height: 90
  },
  MEDIUM_RECTANGLE: {
    width: 300,
    height: 250
  },
  INTERSTITIAL: {
    width: 320,
    height: 480
  }
};

module.exports = {
  adConfig,
  placementTypes,
  placementPositions,
  adSizes
}; 