package com.adverge.backend.repository;

import com.adverge.backend.model.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MetricsRepository extends JpaRepository<Metrics, String> {
    
    List<Metrics> findByPlacementId(String placementId);
    
    List<Metrics> findByPlatform(String platform);
    
    List<Metrics> findByTimestampBetween(Date start, Date end);
    
    List<Metrics> findByPlatformAndTimestampBetween(String platform, Date start, Date end);
    
    List<Metrics> findByPlacementIdAndTimestampBetween(String placementId, Date start, Date end);
    
    /**
     * 根据平台和广告ID查找指标
     * @param platform 平台
     * @param adId 广告ID
     * @return 指标
     */
    Optional<Metrics> findByPlatformAndAdId(String platform, String adId);
} 