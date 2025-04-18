package com.adverge.backend.repository;

import com.adverge.backend.model.Metrics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MetricsRepository extends MongoRepository<Metrics, String> {
    
    List<Metrics> findByPlacementId(String placementId);
    
    List<Metrics> findByPlatform(String platform);
    
    List<Metrics> findByTimestampBetween(Date start, Date end);
    
    List<Metrics> findByPlatformAndTimestampBetween(String platform, Date start, Date end);
    
    List<Metrics> findByPlacementIdAndTimestampBetween(String placementId, Date start, Date end);
} 