package com.adverge.backend.repository;

import com.adverge.backend.model.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<Config, String> {
    
    Optional<Config> findByAppId(String appId);
    
    @Query("SELECT p FROM Config c JOIN c.platforms p WHERE LOWER(p.name) = LOWER(:name)")
    List<Config.Platform> findPlatformByName(@Param("name") String name);
    
    @Query("SELECT p FROM Config c JOIN c.platforms p")
    List<Config.Platform> findAllPlatforms();
} 