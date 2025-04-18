const fs = require('fs');
const path = require('path');

class AdConfigManager {
  constructor() {
    this.configs = {};
    this.loadConfigs();
  }

  loadConfigs() {
    try {
      const configPath = path.join(__dirname, '../config/ad-platforms.json');
      if (fs.existsSync(configPath)) {
        const configData = JSON.parse(fs.readFileSync(configPath, 'utf8'));
        this.configs = configData;
      } else {
        // 默认配置
        this.configs = {
          topon: {
            enabled: true,
            apiUrl: 'https://api.topon.com/v1',
            appId: '',
            appKey: '',
            placementIds: {
              banner: '',
              interstitial: '',
              rewarded: ''
            }
          },
          bigo: {
            enabled: true,
            apiUrl: 'https://api.bigoads.com/v1',
            appId: '',
            appKey: '',
            placementIds: {
              banner: '',
              interstitial: '',
              rewarded: ''
            }
          },
          inmobi: {
            enabled: true,
            apiUrl: 'https://api.inmobi.com/v1',
            accountId: '',
            appKey: '',
            placementIds: {
              banner: '',
              interstitial: '',
              rewarded: ''
            }
          },
          mintegral: {
            enabled: true,
            apiUrl: 'https://api.mintegral.com/v1',
            appId: '',
            appKey: '',
            placementIds: {
              banner: '',
              interstitial: '',
              rewarded: ''
            }
          },
          vungle: {
            enabled: true,
            apiUrl: 'https://api.vungle.com/v1',
            appId: '',
            appKey: '',
            placementIds: {
              banner: '',
              interstitial: '',
              rewarded: ''
            }
          },
          fyber: {
            enabled: true,
            apiUrl: 'https://api.fyber.com/v1',
            appId: '',
            appKey: '',
            placementIds: {
              banner: '',
              interstitial: '',
              rewarded: ''
            }
          },
          chartboost: {
            enabled: true,
            apiUrl: 'https://api.chartboost.com/v1',
            appId: '',
            appKey: '',
            placementIds: {
              banner: '',
              interstitial: '',
              rewarded: ''
            }
          },
          ironsource: {
            enabled: true,
            apiUrl: 'https://api.ironsrc.com/v1',
            appId: '',
            appKey: '',
            placementIds: {
              banner: '',
              interstitial: '',
              rewarded: ''
            }
          },
          unity: {
            enabled: true,
            apiUrl: 'https://api.unity3d.com/v1',
            gameId: '',
            appKey: '',
            placementIds: {
              banner: '',
              interstitial: '',
              rewarded: ''
            }
          },
          mahimeta: {
            enabled: true,
            apiUrl: 'https://api.mahimeta.com/v1',
            appId: '',
            appKey: '',
            placementIds: {
              banner: '',
              interstitial: '',
              rewarded: ''
            }
          }
        };
        this.saveConfigs();
      }
    } catch (error) {
      console.error('加载广告配置失败:', error);
    }
  }

  saveConfigs() {
    try {
      const configPath = path.join(__dirname, '../config/ad-platforms.json');
      fs.writeFileSync(configPath, JSON.stringify(this.configs, null, 2), 'utf8');
    } catch (error) {
      console.error('保存广告配置失败:', error);
    }
  }

  getConfig(platform) {
    return this.configs[platform] || null;
  }

  updateConfig(platform, config) {
    if (this.configs[platform]) {
      this.configs[platform] = {
        ...this.configs[platform],
        ...config
      };
      this.saveConfigs();
      return true;
    }
    return false;
  }

  getPlacementId(platform, adType) {
    const platformConfig = this.getConfig(platform);
    if (platformConfig && platformConfig.placementIds) {
      return platformConfig.placementIds[adType] || null;
    }
    return null;
  }

  isPlatformEnabled(platform) {
    const platformConfig = this.getConfig(platform);
    return platformConfig ? platformConfig.enabled : false;
  }

  enablePlatform(platform) {
    return this.updateConfig(platform, { enabled: true });
  }

  disablePlatform(platform) {
    return this.updateConfig(platform, { enabled: false });
  }

  getAllEnabledPlatforms() {
    return Object.keys(this.configs).filter(platform => this.isPlatformEnabled(platform));
  }

  getPlatformCredentials(platform) {
    const platformConfig = this.getConfig(platform);
    if (!platformConfig) return null;

    const credentials = {
      apiUrl: platformConfig.apiUrl
    };

    // 根据不同平台返回不同的凭证
    switch (platform) {
      case 'inmobi':
        credentials.accountId = platformConfig.accountId;
        credentials.appKey = platformConfig.appKey;
        break;
      case 'unity':
        credentials.gameId = platformConfig.gameId;
        credentials.appKey = platformConfig.appKey;
        break;
      default:
        credentials.appId = platformConfig.appId;
        credentials.appKey = platformConfig.appKey;
    }

    return credentials;
  }
}

module.exports = new AdConfigManager(); 