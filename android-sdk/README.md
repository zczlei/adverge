# AdVerge Android SDK

AdVerge Android SDK是一个综合性的广告聚合SDK，可以集成多个广告平台并提供统一的接口。SDK负责客户端广告展示和用户交互，而广告平台的选择和竞价则由AdVerge后端服务完成。

## 架构概述

AdVerge Android SDK采用模块化设计，主要包含以下组件：

1. **核心模块（Core）**：SDK的入口点和全局配置
2. **网络模块（Network）**：与AdVerge后端服务通信
3. **平台模块（Platform）**：管理不同广告平台的适配器
4. **视图模块（View）**：提供广告展示的UI组件
5. **工具模块（Utils）**：提供辅助功能和工具类

## 数据流程

1. 应用通过SDK请求广告
2. SDK发送请求到AdVerge后端服务
3. 后端服务进行竞价并选择最优广告平台
4. SDK收到响应后加载对应平台的广告
5. 广告展示给用户并回传事件数据

## 目录结构和文件说明

### 核心模块

- **`AdSDK.java`**: SDK的主入口类，提供全局初始化和配置方法。管理广告平台适配器的注册，并提供访问其他组件的接口。

### 适配器模块 (adapter)

- **`AdMobAdapter.java`**: Google AdMob广告平台的适配器，实现了AdPlatformAdapter接口，封装了AdMob SDK的初始化、广告加载和显示。
- **`UnityAdapter.java`**: Unity Ads广告平台的适配器，封装了Unity Ads SDK的操作，处理横幅、插屏和激励广告。

### 配置模块 (config)

- **`AdConfig.java`**: SDK配置类，存储SDK运行所需的全局配置信息，如应用ID、服务器URL等。

### 模型模块 (model)

- **`AdRequest.java`**: 广告请求模型类，封装了发送到后端的请求参数，包括广告位ID、设备信息等。
- **`AdResponse.java`**: 广告响应模型类，解析后端返回的广告数据，包括胜出平台、广告ID等。
- **`Platform.java`**: 广告平台配置模型类，存储平台名称、ID、描述和配置参数等信息。

### 视图模块 (view)

- **`AdView.java`**: 广告视图基类，定义了加载和显示广告的通用方法，以及事件通知机制。
- **`BannerAd.java`**: 横幅广告视图，继承自AdView，实现了横幅广告的加载和显示。
- **`InterstitialAd.java`**: 插屏广告视图，实现了全屏插屏广告的加载和显示。
- **`RewardedAd.java`**: 激励广告视图，实现了带奖励的广告的加载和显示，并提供奖励回调。
- **`NativeAd.java`**: 原生广告视图，支持自定义布局的原生广告。

### 平台模块 (platform)

- **`AdPlatformAdapter.java`**: 广告平台适配器接口，定义了广告平台需要实现的方法，如初始化、加载和显示广告。
- **`AdPlatformManager.java`**: 管理所有广告平台适配器的单例类，负责注册、查找和移除适配器。

### 网络模块 (network)

- **`AdServerClient.java`**: 广告服务器客户端接口，定义了与后端服务通信的方法，如请求广告、上报事件等。
- **`AdServerClientImpl.java`**: AdServerClient接口的实现类，使用OkHttp库与后端服务通信。
- **`AdCallback.java`**: 广告网络请求回调接口，处理成功和失败的回调。

### 服务器模块 (server)

- **`AdServer.java`**: 封装了与AdVerge服务器的通信逻辑，处理广告请求和竞价选择。

### 管理器模块 (manager)

- **`AdAggregator.java`**: 广告聚合管理器，协调多个广告平台之间的关系，支持Header Bidding和Waterfall策略。

### 监听器模块 (listener)

- **`AdListener.java`**: 广告事件监听器接口，定义了广告生命周期事件的回调方法，如加载成功、失败、展示等。

### 工具模块 (utils)

- **`AdLifecycleMonitor.java`**: 监控广告生命周期的工具类，跟踪广告的加载、展示和过期。
- **`AdPreloadManager.java`**: 广告预加载管理器，提前加载广告以提高用户体验。
- **`BuildConfig.java`**: 构建配置类，存储版本号、调试模式等编译时常量。
- **`Logger.java`**: 日志工具类，统一管理SDK的日志输出。
- **`RetryManager.java`**: 重试管理器，处理广告请求失败时的重试逻辑。
- **`SecurityUtils.java`**: 安全工具类，提供加密和解密功能，保护通信数据。

## 使用指南

### 初始化SDK

```java
// 初始化SDK
AdSDK.initialize(context);

// 可选：配置SDK
AdConfig config = new AdConfig();
config.setAppId("your_app_id");
config.setBaseUrl("https://api.adverge.com");
AdSDK.getInstance().setConfig(config);
```

### 横幅广告

```java
// 在布局文件中添加BannerAd
<com.adverge.sdk.view.BannerAd
    android:id="@+id/bannerAd"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />

// 在代码中加载广告
BannerAd bannerAd = findViewById(R.id.bannerAd);
bannerAd.setAdUnitId("your_ad_unit_id");
bannerAd.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded() {
        // 广告加载成功
    }

    @Override
    public void onAdLoadFailed(String error) {
        // 广告加载失败
    }

    // 其他回调方法...
});
bannerAd.loadAd();
```

### 插屏广告

```java
// 创建并加载插屏广告
InterstitialAd interstitialAd = new InterstitialAd(context);
interstitialAd.setAdUnitId("your_ad_unit_id");
interstitialAd.setAdListener(new AdListener() {
    // 实现回调方法...
});
interstitialAd.loadAd();

// 广告准备好后显示
if (interstitialAd.isLoaded()) {
    interstitialAd.show();
}
```

### 激励广告

```java
// 创建并加载激励广告
RewardedAd rewardedAd = new RewardedAd(context);
rewardedAd.setAdUnitId("your_ad_unit_id");
rewardedAd.setAdListener(new AdListener() {
    @Override
    public void onRewarded(String type, int amount) {
        // 发放奖励
    }
    // 其他回调方法...
});
rewardedAd.loadAd();

// 广告准备好后显示
if (rewardedAd.isLoaded()) {
    rewardedAd.show();
}
```

## 高级功能

### 广告预加载

```java
// 预加载广告
AdPreloadManager.getInstance(context).preloadAd(adUnitId, getAdType());

// 获取预加载的广告
AdResponse preloadedAd = AdPreloadManager.getInstance(context).getPreloadedAd(adUnitId);
```

### 性能追踪

```java
// 跟踪广告性能
Map<String, Object> params = new HashMap<>();
params.put("loadTime", loadTime);
params.put("renderTime", renderTime);
AdSDK.getInstance().getAdServerClient().trackPerformance(adId, "render_complete", params);
```

## 注意事项

1. SDK需要以下权限：
   - `android.permission.INTERNET`
   - `android.permission.ACCESS_NETWORK_STATE`

2. 集成第三方SDK时，请确保按照各平台的要求添加必要的依赖和配置。

3. 在应用的AndroidManifest.xml中添加必要的配置:
   ```xml
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy"/>
   ```

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