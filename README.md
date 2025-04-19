# AdVerge Android SDK

AdVerge 是一个广告聚合 SDK，支持多个广告平台的广告请求和展示。

## 功能特性

- 支持多个广告平台
  - TopOn
  - Bigo Ads
  - InMobi
  - Mintegral
  - Vungle
  - Fyber
  - Chartboost
  - IronSource
  - Unity
  - Mahimeta
- 支持多种广告类型
  - 横幅广告
  - 插页广告
  - 激励广告
- 智能竞价策略
- 实时数据统计
- 性能监控

## 快速开始

### 1. 添加依赖

在项目的 `build.gradle` 文件中添加依赖：

```gradle
dependencies {
    implementation 'com.adverge:adverge-sdk:1.0.0'
}
```

### 2. 初始化 SDK

在 `Application` 类中初始化 SDK：

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化 SDK
        AdVergeSdk.init(this, new AdVergeConfig.Builder()
            .setAppId("your_app_id")
            .setAppKey("your_app_key")
            .setTestMode(true)
            .build());
    }
}
```

### 3. 配置广告平台

在后台管理系统中配置广告平台信息：

1. 访问应用管理页面 (`/app-management.html`)
   - 创建应用
   - 配置应用信息（名称、包名等）

2. 访问广告位管理页面 (`/ad-management.html`)
   - 创建广告位
   - 配置广告位信息（名称、ID、类型等）
   - 关联应用

### 4. 使用广告

#### 横幅广告

```java
// 创建横幅广告
BannerAd bannerAd = new BannerAd(this, "your_ad_unit_id");
bannerAd.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded() {
        // 广告加载成功
    }
    
    @Override
    public void onAdFailedToLoad(AdError error) {
        // 广告加载失败
    }
    
    @Override
    public void onAdClicked() {
        // 广告被点击
    }
});

// 加载广告
bannerAd.loadAd();

// 展示广告
bannerAd.show();
```

#### 插页广告

```java
// 创建插页广告
InterstitialAd interstitialAd = new InterstitialAd(this, "your_ad_unit_id");
interstitialAd.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded() {
        // 广告加载成功
    }
    
    @Override
    public void onAdFailedToLoad(AdError error) {
        // 广告加载失败
    }
    
    @Override
    public void onAdClosed() {
        // 广告关闭
    }
});

// 加载广告
interstitialAd.loadAd();

// 展示广告
interstitialAd.show();
```

#### 激励广告

```java
// 创建激励广告
RewardedAd rewardedAd = new RewardedAd(this, "your_ad_unit_id");
rewardedAd.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded() {
        // 广告加载成功
    }
    
    @Override
    public void onAdFailedToLoad(AdError error) {
        // 广告加载失败
    }
    
    @Override
    public void onRewarded(RewardItem reward) {
        // 用户获得奖励
    }
});

// 加载广告
rewardedAd.loadAd();

// 展示广告
rewardedAd.show();
```

## 配置说明

### 应用配置

在应用管理页面配置应用信息：
- 应用名称
- 包名
- 描述
- 启用状态

### 广告位配置

在广告位管理页面配置广告位信息：
- 广告位名称
- 广告位ID
- 广告类型
- 关联应用
- 描述
- 启用状态

## 注意事项

1. 确保在 AndroidManifest.xml 中添加必要的权限
2. 在合适的生命周期中处理广告对象
3. 注意广告加载和展示的时机
4. 处理广告加载失败的情况
5. 遵守各广告平台的展示规则

## 常见问题

1. 广告加载失败
   - 检查网络连接
   - 确认广告位ID是否正确
   - 查看广告平台配置是否完整

2. 广告展示异常
   - 检查广告对象是否已加载
   - 确认广告位类型是否匹配
   - 查看广告平台状态是否正常

## 技术支持

如有问题，请联系技术支持：
- 邮箱：support@adverge.com
- 电话：400-xxx-xxxx 

## 数据流向

Android App -> AdVerge SDK -> 后端服务 -> 广告平台竞价 -> 后端服务 -> AdVerge SDK -> 广告平台SDK -> 展示广告 