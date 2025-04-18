package com.adverge.backend.config;

import com.adverge.backend.model.Config;
import com.adverge.backend.repository.ConfigRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class AdNetworkConfig {

    @Bean
    public Config adConfig(ConfigRepository configRepository) {
        // 检查是否已有配置
        List<Config> existingConfigs = configRepository.findAll();
        if (!existingConfigs.isEmpty()) {
            return existingConfigs.get(0);
        }
        
        // 创建默认配置
        Config config = new Config();
        config.setId(java.util.UUID.randomUUID().toString());
        config.setPlatforms(createDefaultPlatforms());
        return configRepository.save(config);
    }
    
    private List<Config.Platform> createDefaultPlatforms() {
        // BigoAds
        Config.Platform bigoAds = new Config.Platform();
        bigoAds.setName("BigoAds");
        bigoAds.setEnabled(true);
        bigoAds.setAppId("bigo_app_id");
        bigoAds.setAppKey("bigo_app_key");
        bigoAds.setPlacementId("bigo_placement_id");
        bigoAds.setBidFloor(0.5);
        
        // InMobi
        Config.Platform inMobi = new Config.Platform();
        inMobi.setName("InMobi");
        inMobi.setEnabled(true);
        inMobi.setAppId("inmobi_app_id");
        inMobi.setAppKey("inmobi_app_key");
        inMobi.setPlacementId("inmobi_placement_id");
        inMobi.setBidFloor(0.7);
        
        // TopOn
        Config.Platform topOn = new Config.Platform();
        topOn.setName("TopOn");
        topOn.setEnabled(true);
        topOn.setAppId("topon_app_id");
        topOn.setAppKey("topon_app_key");
        topOn.setPlacementId("topon_placement_id");
        topOn.setBidFloor(0.6);
        
        // Mintegral
        Config.Platform mintegral = new Config.Platform();
        mintegral.setName("Mintegral");
        mintegral.setEnabled(true);
        mintegral.setAppId("mintegral_app_id");
        mintegral.setAppKey("mintegral_app_key");
        mintegral.setPlacementId("mintegral_placement_id");
        mintegral.setBidFloor(0.45);
        
        // UnityAds
        Config.Platform unityAds = new Config.Platform();
        unityAds.setName("UnityAds");
        unityAds.setEnabled(true);
        unityAds.setAppId("unity_app_id");
        unityAds.setAppKey("unity_app_key");
        unityAds.setPlacementId("unity_placement_id");
        unityAds.setBidFloor(0.55);
        
        // AppLovin
        Config.Platform appLovin = new Config.Platform();
        appLovin.setName("AppLovin");
        appLovin.setEnabled(true);
        appLovin.setAppId("applovin_app_id");
        appLovin.setAppKey("applovin_sdk_key");
        appLovin.setPlacementId("applovin_zone_id");
        appLovin.setBidFloor(0.65);
        
        // IronSource
        Config.Platform ironSource = new Config.Platform();
        ironSource.setName("IronSource");
        ironSource.setEnabled(true);
        ironSource.setAppId("ironsource_app_id");
        ironSource.setAppKey("ironsource_app_key");
        ironSource.setPlacementId("ironsource_placement_id");
        ironSource.setBidFloor(0.58);
        
        // Fyber
        Config.Platform fyber = new Config.Platform();
        fyber.setName("Fyber");
        fyber.setEnabled(true);
        fyber.setAppId("fyber_app_id");
        fyber.setAppKey("fyber_app_key");
        fyber.setPlacementId("fyber_placement_id");
        fyber.setBidFloor(0.52);
        
        return Arrays.asList(bigoAds, inMobi, topOn, mintegral, unityAds, appLovin, ironSource, fyber);
    }
} 