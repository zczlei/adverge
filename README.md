# AdVerge 广告聚合平台

## 1. 系统总体说明

### 1.1 系统架构

```
APP端 <-> Adverge SDK <-> Adverge服务器 <-> 广告平台
```

### 1.2 核心功能

- 多广告平台集成
- 实时竞价（RTB）功能
- 广告预加载
- 性能监控和优化
- 错误处理和重试机制

### 1.3 支持平台

- TopOn
- BigoAds
- Unity Ads
- 其他主流广告平台

### 1.4 系统要求

- Android SDK: Android 5.0 (API 21) 及以上
- Web SDK: 支持现代浏览器
- 服务器: Node.js 14.0 及以上

## 2. 前端 SDK 使用说明

### 2.1 Android SDK

#### 2.1.1 集成步骤

1. **添加依赖**
   ```gradle
   implementation 'com.adverge:sdk:1.0.0'
   ```

2. **初始化SDK**
   ```java
   public class MyApplication extends Application {
       @Override
       public void onCreate() {
           super.onCreate();
           AdSDK.initialize(this, "your_app_id");
       }
   }
   ```

3. **配置广告平台**
   ```java
   AdSDK.getInstance().setPlatformConfig("topon", new TopOnConfig.Builder()
       .setAppId("your_app_id")
       .setAppKey("your_app_key")
       .build());
   ```

#### 2.1.2 广告类型使用

1. **横幅广告**
   ```java
   BannerAdView bannerAd = new BannerAdView(context, "banner_ad_unit_id");
   bannerAd.setAdListener(new AdListener() {
       @Override
       public void onAdLoaded() {
           // 广告加载成功
       }
   });
   bannerAd.loadAd();
   ```

2. **插页广告**
   ```java
   InterstitialAd interstitialAd = new InterstitialAd(context, "interstitial_ad_unit_id");
   interstitialAd.setAdListener(new AdListener() {
       @Override
       public void onAdLoaded() {
           interstitialAd.show();
       }
   });
   interstitialAd.loadAd();
   ```

3. **激励视频广告**
   ```java
   RewardedVideoAd rewardedAd = new RewardedVideoAd(context, "rewarded_ad_unit_id");
   rewardedAd.setAdListener(new AdListener() {
       @Override
       public void onAdLoaded() {
           rewardedAd.show();
       }
       
       @Override
       public void onRewarded() {
           // 发放奖励
       }
   });
   rewardedAd.loadAd();
   ```

#### 2.1.3 高级功能

1. **预加载广告**
   ```java
   AdPreloadManager.getInstance(context).preloadAd(adUnitId, getAdType());
   ```

2. **设置测试模式**
   ```java
   AdSDK.getInstance().setTestMode(true);
   ```

3. **自定义广告请求**
   ```java
   AdRequest request = new AdRequest.Builder()
       .setAdUnitId("your_ad_unit_id")
       .setAdType(AdType.BANNER)
       .setWidth(320)
       .setHeight(50)
       .build();
   ```

### 2.2 Web SDK

#### 2.2.1 集成步骤

1. **引入SDK**
   ```html
   <script src="https://cdn.adverge.com/sdk/adverge-sdk.min.js"></script>
   ```

2. **初始化SDK**
   ```javascript
   AdvergeSDK.init({
       appId: 'your_app_id',
       testMode: false
   });
   ```

#### 2.2.2 广告类型使用

1. **横幅广告**
   ```javascript
   const bannerAd = new AdvergeSDK.BannerAd('banner_ad_unit_id');
   bannerAd.load()
       .then(() => {
           bannerAd.show(document.getElementById('ad-container'));
       });
   ```

2. **插页广告**
   ```javascript
   const interstitialAd = new AdvergeSDK.InterstitialAd('interstitial_ad_unit_id');
   interstitialAd.load()
       .then(() => {
           interstitialAd.show();
       });
   ```

3. **激励视频广告**
   ```javascript
   const rewardedAd = new AdvergeSDK.RewardedAd('rewarded_ad_unit_id');
   rewardedAd.load()
       .then(() => {
           rewardedAd.show();
       });
   ```

## 3. 后端服务器说明

### 3.1 环境要求

- Node.js 14.0 及以上
- Redis 6.0 及以上
- MongoDB 4.4 及以上

### 3.2 配置说明

1. **环境变量配置**
   ```env
   # 服务器配置
   PORT=3000
   NODE_ENV=production
   
   # 数据库配置
   REDIS_URL=redis://localhost:6379
   MONGO_URL=mongodb://localhost:27017
   
   # 日志配置
   LOG_LEVEL=info
   
   # 广告平台配置
   TOPON_APP_ID=your_app_id
   TOPON_APP_KEY=your_app_key
   ```

2. **平台配置**
   ```json
   {
     "topon": {
       "appId": "your_app_id",
       "appKey": "your_app_key"
     },
     "bigoAds": {
       "appId": "your_app_id",
       "appKey": "your_app_key"
     }
   }
   ```

### 3.3 API 接口

1. **广告请求接口**
   ```
   POST /api/v1/ad/request
   ```

2. **竞价接口**
   ```
   POST /api/v1/bid/process
   ```

3. **胜出通知接口**
   ```
   POST /api/v1/bid/win
   ```

### 3.4 部署说明

1. **安装依赖**
   ```bash
   npm install
   ```

2. **启动服务**
   ```bash
   npm start
   ```

3. **PM2 部署**
   ```bash
   pm2 start ecosystem.config.js
   ```

### 3.5 监控和维护

1. **性能监控**
   ```javascript
   // 记录广告加载时间
   const startTime = Date.now();
   // ... 加载广告 ...
   const loadTime = Date.now() - startTime;
   monitoringService.recordPerformance('ad_load_time', loadTime);
   ```

2. **错误处理**
   ```javascript
   try {
       // 广告操作
   } catch (error) {
       logger.error('广告操作失败:', error);
       // 重试逻辑
   }
   ```

3. **日志管理**
   - 使用 ELK 栈进行日志收集和分析
   - 设置日志轮转策略
   - 配置告警规则

## 4. 更新日志

### v1.0.0
- 初始版本发布
- 支持 TopOn、BigoAds 等平台
- 实现实时竞价功能
- 添加性能监控

### v1.1.0 (计划中)
- 新增 Unity Ads 支持
- 优化缓存机制
- 增强错误处理
- 添加 A/B 测试功能 