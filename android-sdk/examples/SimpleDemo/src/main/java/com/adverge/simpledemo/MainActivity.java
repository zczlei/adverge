package com.adverge.simpledemo;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.adverge.sdk.AdSDK;
import com.adverge.sdk.listener.AdListener;
import com.adverge.sdk.model.AdRequest;
import com.adverge.sdk.view.BannerAd;
import com.adverge.sdk.view.InterstitialAd;
import com.adverge.sdk.view.RewardedAd;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FrameLayout bannerContainer;
    private Button loadBannerButton;
    private Button loadInterstitialButton;
    private Button showInterstitialButton;
    private Button loadRewardedButton;
    private Button showRewardedButton;
    private TextView statusTextView;

    private BannerAd bannerAd;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化AdSDK
        updateStatus(getString(R.string.status_init));
        AdSDK.initialize(this);
        
        // 配置SDK
        Map<String, Object> configs = new HashMap<>();
        configs.put("admob", BuildConfig.APP_ID);
        AdSDK.getInstance().getPlatformManager().initAll(this, configs);
        updateStatus(getString(R.string.status_init_complete));

        // 初始化视图
        bannerContainer = findViewById(R.id.banner_container);
        loadBannerButton = findViewById(R.id.load_banner_button);
        loadInterstitialButton = findViewById(R.id.load_interstitial_button);
        showInterstitialButton = findViewById(R.id.show_interstitial_button);
        loadRewardedButton = findViewById(R.id.load_rewarded_button);
        showRewardedButton = findViewById(R.id.show_rewarded_button);
        statusTextView = findViewById(R.id.status_text);

        // 初始化广告实例
        initBannerAd();
        initInterstitialAd();
        initRewardedAd();

        // 设置按钮点击监听器
        setupButtonListeners();
    }

    private void initBannerAd() {
        bannerAd = new BannerAd(this);
        bannerAd.setAdUnitId(BuildConfig.BANNER_AD_UNIT_ID);
        bannerAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                updateStatus("横幅广告加载成功");
                bannerContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdLoadFailed(String errorMessage) {
                updateStatus("横幅广告加载失败: " + errorMessage);
            }
            
            @Override
            public void onAdShown() {
                updateStatus("横幅广告显示");
            }

            @Override
            public void onAdClicked() {
                updateStatus("横幅广告被点击");
            }

            @Override
            public void onAdClosed() {
                updateStatus("横幅广告关闭");
            }
            
            @Override
            public void onRewarded(String type, int amount) {
                // 横幅广告不会有奖励
            }
        });
    }

    private void initInterstitialAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(BuildConfig.INTERSTITIAL_AD_UNIT_ID);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                updateStatus("插屏广告加载成功");
                showInterstitialButton.setEnabled(true);
            }

            @Override
            public void onAdLoadFailed(String errorMessage) {
                updateStatus("插屏广告加载失败: " + errorMessage);
                showInterstitialButton.setEnabled(false);
            }
            
            @Override
            public void onAdShown() {
                updateStatus("插屏广告显示");
            }

            @Override
            public void onAdClicked() {
                updateStatus("插屏广告被点击");
            }

            @Override
            public void onAdClosed() {
                updateStatus("插屏广告关闭");
                // 可以在这里自动预加载下一个广告
                loadInterstitialAd();
            }
            
            @Override
            public void onRewarded(String type, int amount) {
                // 插屏广告不会有奖励
            }
        });
    }

    private void initRewardedAd() {
        rewardedAd = new RewardedAd(this);
        rewardedAd.setAdUnitId(BuildConfig.REWARDED_AD_UNIT_ID);
        rewardedAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                updateStatus("激励广告加载成功");
                showRewardedButton.setEnabled(true);
            }

            @Override
            public void onAdLoadFailed(String errorMessage) {
                updateStatus("激励广告加载失败: " + errorMessage);
                showRewardedButton.setEnabled(false);
            }
            
            @Override
            public void onAdShown() {
                updateStatus("激励广告开始展示");
            }

            @Override
            public void onAdClicked() {
                updateStatus("激励广告被点击");
            }

            @Override
            public void onAdClosed() {
                updateStatus("激励广告关闭");
                // 可以在这里自动预加载下一个广告
                loadRewardedAd();
            }
            
            @Override
            public void onRewarded(String type, int amount) {
                updateStatus("获得奖励: " + amount + " " + type);
                Toast.makeText(MainActivity.this, 
                               "恭喜获得 " + amount + " " + type + " 奖励!", 
                               Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupButtonListeners() {
        loadBannerButton.setOnClickListener(v -> loadBannerAd());
        loadInterstitialButton.setOnClickListener(v -> loadInterstitialAd());
        showInterstitialButton.setOnClickListener(v -> showInterstitialAd());
        loadRewardedButton.setOnClickListener(v -> loadRewardedAd());
        showRewardedButton.setOnClickListener(v -> showRewardedAd());
    }

    private void loadBannerAd() {
        updateStatus("正在加载横幅广告...");
        bannerContainer.removeAllViews();
        
        // 创建请求
        AdRequest request = new AdRequest(BuildConfig.BANNER_AD_UNIT_ID);
            
        // 加载广告并添加到容器
        bannerAd.loadAd(request);
        bannerContainer.addView(bannerAd);
    }

    private void loadInterstitialAd() {
        updateStatus("正在加载插屏广告...");
        showInterstitialButton.setEnabled(false);
        interstitialAd.loadAd();
    }

    private void showInterstitialAd() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            updateStatus("插屏广告尚未准备好");
            Toast.makeText(this, "插屏广告尚未准备好", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRewardedAd() {
        updateStatus("正在加载激励广告...");
        showRewardedButton.setEnabled(false);
        rewardedAd.loadAd();
    }

    private void showRewardedAd() {
        if (rewardedAd != null && rewardedAd.isLoaded()) {
            rewardedAd.show();
        } else {
            updateStatus("激励广告尚未准备好");
            Toast.makeText(this, "激励广告尚未准备好", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatus(String message) {
        if (Thread.currentThread() != getMainLooper().getThread()) {
            runOnUiThread(() -> updateStatus(message));
            return;
        }
        
        statusTextView.setText(message);
        // 5秒后清除状态消息
        new Handler().postDelayed(() -> {
            if (statusTextView.getText().toString().equals(message)) {
                statusTextView.setText(R.string.ready);
            }
        }, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源
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
} 