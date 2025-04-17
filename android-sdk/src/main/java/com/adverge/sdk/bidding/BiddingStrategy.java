package bidding;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BiddingStrategy {
    private static final Logger logger = Logger.getLogger(BiddingStrategy.class.getName());
    
    // 平台历史表现权重
    private Map<String, Double> platformWeights;
    // 平台历史指标
    private Map<String, PlatformMetrics> platformMetrics;
    
    public BiddingStrategy() {
        platformWeights = new HashMap<>();
        platformMetrics = new HashMap<>();
        // 初始化默认权重
        initializeDefaultWeights();
    }
    
    private void initializeDefaultWeights() {
        platformWeights.put("topon", 1.0);
        platformWeights.put("bigo", 1.0);
        platformWeights.put("unity", 1.0);
        // 添加其他平台...
    }
    
    // 基于历史数据动态调整权重
    public void adjustWeights(PlatformMetrics metrics) {
        String platform = metrics.getPlatform();
        
        // 计算各项指标的权重
        double fillRateWeight = metrics.getFillRate() * 0.3;
        double ecpmWeight = metrics.getAverageEcpm() * 0.4;
        double loadTimeWeight = (1 - metrics.getAverageLoadTime() / 1000) * 0.2;
        double ctrWeight = metrics.getClickThroughRate() * 0.1;
        
        // 计算总权重
        double totalWeight = fillRateWeight + ecpmWeight + loadTimeWeight + ctrWeight;
        
        // 更新平台权重
        platformWeights.put(platform, totalWeight);
        
        // 更新平台指标
        platformMetrics.put(platform, metrics);
        
        logger.info("Adjusted weights for platform: " + platform + ", new weight: " + totalWeight);
    }
    
    // 获取平台权重
    public double getPlatformWeight(String platform) {
        return platformWeights.getOrDefault(platform, 1.0);
    }
    
    // 计算最终竞价
    public double calculateFinalBid(String platform, double baseBid) {
        double weight = getPlatformWeight(platform);
        return baseBid * weight;
    }
    
    // 获取平台指标
    public PlatformMetrics getPlatformMetrics(String platform) {
        return platformMetrics.get(platform);
    }
} 