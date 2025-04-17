# AdVerge Android SDK 使用说明

## 1. 集成说明

### 1.1 添加依赖
在项目的 `build.gradle` 文件中添加以下依赖：

```gradle
dependencies {
    implementation 'com.adverge:adverge-sdk:1.0.0'
}
```

### 1.2 初始化SDK
在 Application 类中初始化 SDK：

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化SDK
        AdSDK.initialize(this, new AdSDKConfig.Builder()
                .setAppId("your_app_id")
                .setDebugMode(true)  // 开发阶段建议开启
                .build());
                
        // 初始化其他组件
        ImageLoader.initialize(this);
        AdPreloadManager.initialize(this);
    }
}
```

## 2. 广告类型

### 2.1 横幅广告 (BannerAdView)
```java
// 创建横幅广告
BannerAdView bannerAd = new BannerAdView(context, "your_ad_unit_id");
bannerAd.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded(AdResponse response) {
        // 广告加载成功
    }
    
    @Override
    public void onAdFailedToLoad(String error) {
        // 广告加载失败
    }
    
    @Override
    public void onAdShown() {
        // 广告展示
    }
    
    @Override
    public void onAdClicked() {
        // 广告被点击
    }
    
    @Override
    public void onAdClosed() {
        // 广告关闭
    }
});

// 加载广告
bannerAd.loadAd();

// 添加到布局
container.addView(bannerAd);

// 设置自动刷新间隔（可选）
bannerAd.setAutoRefreshInterval(30); // 30秒
```

### 2.2 插页广告 (InterstitialAd)
```java
// 创建插页广告
InterstitialAd interstitialAd = new InterstitialAd(context, "your_ad_unit_id");
interstitialAd.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded(AdResponse response) {
        // 广告加载成功
    }
    
    @Override
    public void onAdFailedToLoad(String error) {
        // 广告加载失败
    }
    
    @Override
    public void onAdShown() {
        // 广告展示
    }
    
    @Override
    public void onAdClicked() {
        // 广告被点击
    }
    
    @Override
    public void onAdClosed() {
        // 广告关闭
    }
});

// 加载广告
interstitialAd.loadAd();

// 展示广告
if (interstitialAd.isLoaded()) {
    interstitialAd.show();
}
```

### 2.3 激励广告 (RewardedAd)
```java
// 创建激励广告
RewardedAd rewardedAd = new RewardedAd(context, "your_ad_unit_id");
rewardedAd.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded(AdResponse response) {
        // 广告加载成功
    }
    
    @Override
    public void onAdFailedToLoad(String error) {
        // 广告加载失败
    }
    
    @Override
    public void onAdShown() {
        // 广告展示
    }
    
    @Override
    public void onAdClicked() {
        // 广告被点击
    }
    
    @Override
    public void onAdClosed() {
        // 广告关闭
    }
    
    @Override
    public void onAdRewarded() {
        // 用户获得奖励
    }
});

// 加载广告
rewardedAd.loadAd();

// 展示广告
if (rewardedAd.isLoaded()) {
    rewardedAd.show();
}
```

### 2.4 原生广告 (NativeAd)
```java
// 创建原生广告
NativeAd nativeAd = new NativeAd(context, "your_ad_unit_id");
nativeAd.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded(AdResponse response) {
        // 广告加载成功
        // 更新UI
        titleView.setText(response.getTitle());
        descriptionView.setText(response.getDescription());
        ImageLoader.getInstance().loadImage(response.getIconUrl(), iconView);
        ImageLoader.getInstance().loadImage(response.getImageUrl(), imageView);
    }
    
    @Override
    public void onAdFailedToLoad(String error) {
        // 广告加载失败
    }
    
    @Override
    public void onAdShown() {
        // 广告展示
    }
    
    @Override
    public void onAdClicked() {
        // 广告被点击
    }
    
    @Override
    public void onAdClosed() {
        // 广告关闭
    }
});

// 加载广告
nativeAd.loadAd();

// 添加到布局
container.addView(nativeAd);
```

## 3. 高级功能

### 3.1 广告预加载
```java
// 预加载广告
AdPreloadManager.getInstance().preloadAd("your_ad_unit_id", "banner", new AdListener() {
    @Override
    public void onAdLoaded(AdResponse response) {
        // 预加载成功
    }
    
    @Override
    public void onAdFailedToLoad(String error) {
        // 预加载失败
    }
});

// 获取预加载的广告
AdResponse preloadedAd = AdPreloadManager.getInstance().getPreloadedAd("your_ad_unit_id");
```

### 3.2 广告生命周期监控
```java
// 注册广告视图进行监控
AdLifecycleMonitor.getInstance().registerAdView(adView, adListener);

// 获取广告数据
int impressions = AdLifecycleMonitor.getInstance().getImpressionCount("your_ad_unit_id");
int clicks = AdLifecycleMonitor.getInstance().getClickCount("your_ad_unit_id");
float ctr = AdLifecycleMonitor.getInstance().getClickThroughRate("your_ad_unit_id");
long duration = AdLifecycleMonitor.getInstance().getAverageImpressionDuration("your_ad_unit_id");
```

### 3.3 请求重试
```java
// 使用重试管理器执行广告请求
RetryManager.getInstance().executeWithRetry(adRequest, new AdListener() {
    @Override
    public void onAdLoaded(AdResponse response) {
        // 请求成功
    }
    
    @Override
    public void onAdFailedToLoad(String error) {
        // 请求失败
    }
});

// 取消重试
RetryManager.getInstance().cancelRetry("your_ad_unit_id");
```

## 4. 注意事项

1. 确保在 Application 中正确初始化 SDK
2. 广告加载失败时，建议使用重试机制
3. 合理使用预加载功能，避免资源浪费
4. 注意广告的生命周期管理，及时销毁不需要的广告
5. 在 Activity 或 Fragment 销毁时，记得调用广告的 destroy() 方法

## 5. 常见问题

### 5.1 广告加载失败
- 检查网络连接
- 确认广告单元 ID 是否正确
- 检查广告配置是否正确
- 查看日志中的具体错误信息

### 5.2 广告展示问题
- 确保广告已成功加载
- 检查广告视图是否正确添加到布局中
- 确认广告尺寸是否符合要求

### 5.3 性能优化
- 合理使用预加载功能
- 及时清理不需要的广告资源
- 避免频繁创建和销毁广告实例

## 6. 技术支持

如有任何问题，请联系技术支持：
- 邮箱：support@adverge.com
- 电话：400-xxx-xxxx 