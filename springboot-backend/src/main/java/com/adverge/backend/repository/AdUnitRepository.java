package com.adverge.backend.repository;

import com.adverge.backend.model.AdUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdUnitRepository extends JpaRepository<AdUnit, String> {
    
    List<AdUnit> findByAppId(String appId);
    
    Optional<AdUnit> findByAppIdAndName(String appId, String name);
    
    List<AdUnit> findByType(String type);
    
    List<AdUnit> findByActive(boolean active);
    
    List<AdUnit> findByAppIdAndActive(String appId, boolean active);
} 