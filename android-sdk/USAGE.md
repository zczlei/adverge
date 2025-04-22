# AdVerge Android SDK 使用指南

本文档提供AdVerge Android SDK的详细使用示例和最佳实践，帮助开发者快速集成和优化广告展示效果。

## 目录

- [安装](#安装)
- [初始化](#初始化)
- [广告类型](#广告类型)
  - [横幅广告](#横幅广告)
  - [插屏广告](#插屏广告)
  - [激励广告](#激励广告)
  - [原生广告](#原生广告)
- [事件监听](#事件监听)
- [广告预加载](#广告预加载)
- [错误处理](#错误处理)
- [性能优化](#性能优化)
- [混淆配置](#混淆配置)
- [常见问题](#常见问题)

## 安装

### Gradle依赖

在应用模块的`build.gradle`文件中添加以下依赖：

```gradle
dependencies {
    implementation 'com.adverge:android-sdk:1.0.0'
    
    // 如果使用AdMob平台
    implementation 'com.google.android.gms:play-services-ads:21.5.0'
    
    // 如果使用Unity Ads平台
    implementation 'com.unity3d.ads:unity-ads:4.6.1'
}
```

### 权限配置

在`AndroidManifest.xml`中添加必要的权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 初始化

在应用的`Application`类中初始化SDK：

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化AdVerge SDK
        AdSDK.initialize(this);
        
        // 配置SDK参数
        AdConfig config = new AdConfig.Builder()
            .setAppId("your_app_id")
            .setBaseUrl("https://api.adverge.com")
            .setLogLevel(Logger.LogLevel.DEBUG) // 开发阶段使用DEBUG级别
            .setTestMode(BuildConfig.DEBUG) // 测试模式
            .build();
            
        AdSDK.getInstance().setConfig(config);
    }
}
```

## 广告类型

### 横幅广告

#### XML布局方式

```xml
<!-- 在布局文件中添加 -->
<com.adverge.sdk.view.BannerAd
    android:id="@+id/banner_ad"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:adUnitId="your_banner_ad_unit_id" />
```

```java
// 在Activity或Fragment中
BannerAd bannerAd = findViewById(R.id.banner_ad);
bannerAd.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded() {
        Log.d("AdVerge", "Banner ad loaded");
    }
    
    @Override
    public void onAdLoadFailed(String errorMessage) {
        Log.e("AdVerge", "Banner ad failed to load: " + errorMessage);
    }
    
    @Override
    public void onAdClicked() {
        Log.d("AdVerge", "Banner ad clicked");
    }
    
    @Override
    public void onAdImpression() {
        Log.d("AdVerge", "Banner ad impression");
    }
    
    @Override
    public void onAdClosed() {
        Log.d("AdVerge", "Banner ad closed");
    }
});

// 加载广告
bannerAd.loadAd();
```

#### 代码创建方式

```java
BannerAd bannerAd = new BannerAd(this);
bannerAd.setAdUnitId("your_banner_ad_unit_id");
bannerAd.setAdListener(/* 同上 */);

// 添加到布局
LinearLayout adContainer = findViewById(R.id.ad_container);
adContainer.addView(bannerAd);

// 加载广告
bannerAd.loadAd();
```

#### 自定义请求参数

```java
AdRequest request = new AdRequest.Builder()
    .addCustomParameter("user_id", "123456")
    .addCustomParameter("age", "25")
    .addCustomParameter("interests", "sports,technology")
    .build();
    
bannerAd.loadAd(request);
```

### 插屏广告

```java
InterstitialAd interstitialAd = new InterstitialAd(this);
interstitialAd.setAdUnitId("your_interstitial_ad_unit_id");
interstitialAd.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded() {
        Log.d("AdVerge", "Interstitial ad loaded");
        // 广告已加载，可以在合适的时机展示
        showInterstitialAdWhenReady();
    }
    
    @Override
    public void onAdLoadFailed(String errorMessage) {
        Log.e("AdVerge", "Interstitial ad failed to load: " + errorMessage);
    }
    
    @Override
    public void onAdClosed() {
        Log.d("AdVerge", "Interstitial ad closed");
        // 广告被关闭，可以在此处继续游戏或跳转页面
        continueToNextLevel();
    }
    
    // 其他回调方法...
});

// 加载插屏广告
interstitialAd.loadAd();

// 在合适的时机展示插屏广告
private void showInterstitialAdWhenReady() {
    if (interstitialAd != null && interstitialAd.isLoaded()) {
        interstitialAd.show();
    } else {
        Log.d("AdVerge", "Interstitial ad not ready yet");
        // 继续执行应用逻辑
        continueToNextLevel();
    }
}
```

### 激励广告

```java
RewardedAd rewardedAd = new RewardedAd(this);
rewardedAd.setAdUnitId("your_rewarded_ad_unit_id");
rewardedAd.setAdListener(new RewardedAdListener() {
    @Override
    public void onAdLoaded() {
        Log.d("AdVerge", "Rewarded ad loaded");
        // 启用激励广告按钮
        rewardButton.setEnabled(true);
    }
    
    @Override
    public void onAdLoadFailed(String errorMessage) {
        Log.e("AdVerge", "Rewarded ad failed to load: " + errorMessage);
        rewardButton.setEnabled(false);
    }
    
    @Override
    public void onAdClosed() {
        Log.d("AdVerge", "Rewarded ad closed");
        // 广告关闭后，重新加载新广告
        rewardedAd.loadAd();
    }
    
    @Override
    public void onRewarded(String type, int amount) {
        Log.d("AdVerge", "User rewarded: " + amount + " " + type);
        // 给用户发放奖励
        giveRewardToUser(type, amount);
    }
    
    // 其他回调方法...
});

// 加载激励广告
rewardedAd.loadAd();

// 用户点击按钮时展示激励广告
rewardButton.setOnClickListener(view -> {
    if (rewardedAd != null && rewardedAd.isLoaded()) {
        rewardedAd.show();
    } else {
        Toast.makeText(this, "激励广告尚未准备好", Toast.LENGTH_SHORT).show();
        // 重新加载广告
        rewardedAd.loadAd();
    }
});
```

### 原生广告

```java
NativeAd nativeAd = new NativeAd(this);
nativeAd.setAdUnitId("your_native_ad_unit_id");
nativeAd.setAdListener(new NativeAdListener() {
    @Override
    public void onAdLoaded() {
        Log.d("AdVerge", "Native ad loaded");
        // 填充原生广告视图
        populateNativeAdView(nativeAd);
    }
    
    @Override
    public void onAdLoadFailed(String errorMessage) {
        Log.e("AdVerge", "Native ad failed to load: " + errorMessage);
        // 隐藏广告容器
        adContainer.setVisibility(View.GONE);
    }
    
    // 其他回调方法...
});

// 加载原生广告
nativeAd.loadAd();

// 填充原生广告视图的方法
private void populateNativeAdView(NativeAd nativeAd) {
    View adView = getLayoutInflater().inflate(R.layout.native_ad_layout, null);
    
    // 设置标题
    TextView titleView = adView.findViewById(R.id.ad_title);
    titleView.setText(nativeAd.getTitle());
    
    // 设置描述
    TextView descriptionView = adView.findViewById(R.id.ad_description);
    descriptionView.setText(nativeAd.getDescription());
    
    // 设置图标
    ImageView iconView = adView.findViewById(R.id.ad_icon);
    Glide.with(this).load(nativeAd.getIconUrl()).into(iconView);
    
    // 设置主图
    ImageView mainImageView = adView.findViewById(R.id.ad_image);
    Glide.with(this).load(nativeAd.getImageUrl()).into(mainImageView);
    
    // 设置行动按钮
    Button callToActionButton = adView.findViewById(R.id.ad_call_to_action);
    callToActionButton.setText(nativeAd.getCallToAction());
    
    // 注册点击事件
    nativeAd.registerView(adView);
    
    // 添加到容器
    adContainer.removeAllViews();
    adContainer.addView(adView);
    adContainer.setVisibility(View.VISIBLE);
}
```

## 事件监听

### 广告生命周期事件

AdVerge SDK提供了丰富的事件回调，帮助开发者在广告生命周期的各个阶段执行相应的操作：

```java
adView.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded() {
        // 广告加载成功
    }
    
    @Override
    public void onAdLoadFailed(String errorMessage) {
        // 广告加载失败
    }
    
    @Override
    public void onAdClicked() {
        // 用户点击广告
    }
    
    @Override
    public void onAdImpression() {
        // 广告展示并计入一次曝光
    }
    
    @Override
    public void onAdOpened() {
        // 广告被打开（如点击后打开了落地页）
    }
    
    @Override
    public void onAdClosed() {
        // 广告被关闭
    }
    
    @Override
    public void onAdLeftApplication() {
        // 用户因点击广告离开了应用
    }
});
```

### 激励广告特有事件

```java
rewardedAd.setAdListener(new RewardedAdListener() {
    // 继承自AdListener的方法...
    
    @Override
    public void onRewarded(String type, int amount) {
        // 用户完成观看并获得奖励
        Log.d("AdVerge", "奖励类型: " + type + ", 数量: " + amount);
    }
    
    @Override
    public void onRewardedVideoStarted() {
        // 激励视频开始播放
    }
    
    @Override
    public void onRewardedVideoCompleted() {
        // 激励视频播放完成
    }
});
```

## 广告预加载

预加载广告可以提高用户体验，减少等待时间：

```java
// 在应用启动时预加载广告
public void preloadAds() {
    // 预加载插屏广告
    AdPreloadManager.getInstance(this)
        .preloadAd("your_interstitial_ad_unit_id", AdType.INTERSTITIAL);
        
    // 预加载激励广告
    AdPreloadManager.getInstance(this)
        .preloadAd("your_rewarded_ad_unit_id", AdType.REWARDED);
}

// 在需要展示时使用预加载的广告
public void showPreloadedInterstitial() {
    AdResponse preloadedAd = AdPreloadManager.getInstance(this)
        .getPreloadedAd("your_interstitial_ad_unit_id");
        
    if (preloadedAd != null) {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("your_interstitial_ad_unit_id");
        interstitialAd.setAdListener(/* 设置监听器 */);
        
        // 使用预加载的广告数据
        interstitialAd.loadWithPreloadedAd(preloadedAd);
        
        // 立即显示
        interstitialAd.show();
        
        // 显示后立即开始预加载下一个广告
        AdPreloadManager.getInstance(this)
            .preloadAd("your_interstitial_ad_unit_id", AdType.INTERSTITIAL);
    } else {
        // 没有预加载的广告，正常加载
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("your_interstitial_ad_unit_id");
        interstitialAd.setAdListener(/* 设置监听器 */);
        interstitialAd.loadAd();
    }
}
```

## 错误处理

```java
adView.setAdListener(new AdListener() {
    @Override
    public void onAdLoadFailed(String errorMessage) {
        Log.e("AdVerge", "广告加载失败: " + errorMessage);
        
        // 根据错误类型进行处理
        if (errorMessage.contains("NO_FILL")) {
            // 没有广告填充，可以尝试其他广告平台或隐藏广告位
            hideAdContainer();
        } else if (errorMessage.contains("NETWORK_ERROR")) {
            // 网络错误，可以稍后重试
            new Handler().postDelayed(() -> {
                adView.loadAd();
            }, 5000); // 5秒后重试
        } else if (errorMessage.contains("TIMEOUT")) {
            // 超时错误，可以重试或降低请求超时时间
            AdRequest request = new AdRequest.Builder()
                .setTimeout(15000) // 增加超时时间为15秒
                .build();
            adView.loadAd(request);
        } else {
            // 其他错误
            Log.e("AdVerge", "未处理的错误类型: " + errorMessage);
        }
    }
});
```

## 性能优化

### 广告加载时机

```java
// 在用户不活跃的时候预加载广告
public void onUserPause() {
    // 用户暂停游戏或进入菜单时预加载下一个关卡的广告
    AdPreloadManager.getInstance(this)
        .preloadAd("next_level_interstitial_id", AdType.INTERSTITIAL);
}

// 使用生命周期管理器监控应用状态
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    AdLifecycleMonitor.getInstance().register(this, new AdLifecycleMonitor.Callback() {
        @Override
        public void onAppForegrounded() {
            // 应用回到前台时检查广告状态
            checkAndRefreshAds();
        }
        
        @Override
        public void onAppBackgrounded() {
            // 应用进入后台时暂停广告加载
            pauseAdOperations();
        }
    });
}

@Override
protected void onDestroy() {
    super.onDestroy();
    AdLifecycleMonitor.getInstance().unregister(this);
}
```

### 性能跟踪

```java
// 跟踪广告加载性能
long startTime = System.currentTimeMillis();

adView.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded() {
        long loadTime = System.currentTimeMillis() - startTime;
        
        Map<String, Object> params = new HashMap<>();
        params.put("loadTime", loadTime);
        params.put("adType", "banner");
        params.put("adUnitId", adView.getAdUnitId());
        
        AdSDK.getInstance().getAdServerClient()
            .trackPerformance(adView.getLastLoadedAdId(), "load_complete", params);
    }
});
```

## 混淆配置

在`proguard-rules.pro`文件中添加以下配置：

```proguard
# AdVerge SDK
-keep class com.adverge.sdk.** { *; }
-keepclassmembers class com.adverge.sdk.** { *; }

# Google AdMob
-keep class com.google.android.gms.ads.** { *; }

# Unity Ads
-keep class com.unity3d.ads.** { *; }

# 如果使用OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }

# Gson (如果使用)
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
```

## 常见问题

### 广告无法加载

**问题**: 广告请求成功发送，但没有广告返回或显示。

**解决方案**:
1. 检查广告单元ID是否正确
2. 确认应用ID配置正确
3. 检查网络连接
4. 查看日志中的具体错误信息
5. 在测试模式下验证

```java
// 开启详细日志
AdConfig config = new AdConfig.Builder()
    .setLogLevel(Logger.LogLevel.VERBOSE)
    .build();
AdSDK.getInstance().setConfig(config);
```

### 广告点击无响应

**问题**: 用户点击广告但没有反应或不跳转。

**解决方案**:
1. 确保广告视图没有被其他视图遮挡
2. 检查是否正确注册了点击区域
3. 验证设备是否安装了相应的浏览器或应用商店
4. 测试设备上的其他广告是否正常工作

### 应用崩溃问题

**问题**: 显示广告时应用崩溃。

**解决方案**:
1. 确保在UI线程上操作广告视图
2. 检查广告生命周期管理是否正确
3. 在Activity/Fragment销毁时释放广告资源

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (bannerAd != null) {
        bannerAd.destroy();
    }
    if (interstitialAd != null) {
        interstitialAd.destroy();
    }
    if (rewardedAd != null) {
        rewardedAd.destroy();
    }
}
```

### 广告填充率低

**问题**: 广告请求成功但填充率低。

**解决方案**:
1. 检查目标用户群体是否匹配广告需求
2. 尝试添加更多广告平台
3. 优化广告请求参数
4. 调整广告请求的时机和频率

```java
// 添加更多定向信息可能提高填充率
AdRequest request = new AdRequest.Builder()
    .addCustomParameter("age_group", "18-24")
    .addCustomParameter("gender", "male")
    .addCustomParameter("interests", "sports,technology,gaming")
    .build();
    
adView.loadAd(request);
```

### 测试与线上环境切换

**问题**: 如何在测试和生产环境之间切换。

**解决方案**:
使用构建变体或配置文件区分环境

```java
// 根据构建类型配置测试模式
boolean isTestMode = BuildConfig.DEBUG;

AdConfig config = new AdConfig.Builder()
    .setTestMode(isTestMode)
    .setTestDeviceIds(Arrays.asList("your_test_device_id_1", "your_test_device_id_2"))
    .build();
    
AdSDK.getInstance().setConfig(config);
```

---

如有更多问题，请联系AdVerge技术支持团队：support@adverge.com 