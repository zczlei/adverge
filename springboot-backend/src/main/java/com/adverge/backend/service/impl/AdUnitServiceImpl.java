package com.adverge.backend.service.impl;

import com.adverge.backend.dto.AdUnitRequest;
import com.adverge.backend.model.AdUnit;
import com.adverge.backend.model.App;
import com.adverge.backend.repository.AdUnitRepository;
import com.adverge.backend.service.AdUnitService;
import com.adverge.backend.service.AppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdUnitServiceImpl implements AdUnitService {

    private final AdUnitRepository adUnitRepository;
    private final AppService appService;

    @Override
    public List<AdUnit> getAllAdUnits() {
        return adUnitRepository.findAll();
    }

    @Override
    public AdUnit getAdUnitById(String id) {
        return adUnitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("广告位不存在: " + id));
    }

    @Override
    public AdUnit createAdUnit(AdUnitRequest adUnitRequest) {
        // 验证应用是否存在
        App app = appService.getAppById(adUnitRequest.getAppId())
                .orElseThrow(() -> new EntityNotFoundException("应用不存在: " + adUnitRequest.getAppId()));
        
        // 检查广告位名称是否已在该应用下存在
        if (adUnitRepository.findByAppIdAndName(adUnitRequest.getAppId(), adUnitRequest.getName()).isPresent()) {
            throw new IllegalArgumentException("该应用下已存在同名广告位: " + adUnitRequest.getName());
        }
        
        AdUnit adUnit = new AdUnit();
        adUnit.setName(adUnitRequest.getName());
        adUnit.setAppId(adUnitRequest.getAppId());
        adUnit.setType(adUnitRequest.getType());
        adUnit.setDescription(adUnitRequest.getDescription());
        adUnit.setActive(adUnitRequest.isActive());
        adUnit.setFloorPrice(adUnitRequest.getFloorPrice());
        adUnit.setRefreshInterval(adUnitRequest.getRefreshInterval());
        adUnit.setPosition(adUnitRequest.getPosition());
        adUnit.setSize(adUnitRequest.getSize());
        
        AdUnit savedAdUnit = adUnitRepository.save(adUnit);
     
        // 更新应用的广告位ID列表
        appService.addAdUnitId(app.getId(), savedAdUnit.getId());
        
        return savedAdUnit;
    }

    @Override
    public AdUnit updateAdUnit(String id, AdUnitRequest adUnitRequest) {
        AdUnit adUnit = getAdUnitById(id);
        
        // 验证应用是否存在
        App app = appService.getAppById(adUnitRequest.getAppId())
                .orElseThrow(() -> new EntityNotFoundException("应用不存在: " + adUnitRequest.getAppId()));
        
        // 检查广告位名称是否已被同一应用下的其他广告位使用
        adUnitRepository.findByAppIdAndName(adUnitRequest.getAppId(), adUnitRequest.getName())
                .ifPresent(existingAdUnit -> {
                    if (!existingAdUnit.getId().equals(id)) {
                        throw new IllegalArgumentException("该应用下已存在同名广告位: " + adUnitRequest.getName());
                    }
                });
        
        // 如果广告位所属应用发生变化，需要更新应用的广告位ID列表
        if (!adUnit.getAppId().equals(adUnitRequest.getAppId())) {
            // 从旧应用中移除广告位ID
            appService.removeAdUnitId(adUnit.getAppId(), id);
            // 添加到新应用
            appService.addAdUnitId(adUnitRequest.getAppId(), id);
        }
        
        adUnit.setName(adUnitRequest.getName());
        adUnit.setAppId(adUnitRequest.getAppId());
        adUnit.setType(adUnitRequest.getType());
        adUnit.setDescription(adUnitRequest.getDescription());
        adUnit.setActive(adUnitRequest.isActive());
        adUnit.setFloorPrice(adUnitRequest.getFloorPrice());
        adUnit.setRefreshInterval(adUnitRequest.getRefreshInterval());
        adUnit.setPosition(adUnitRequest.getPosition());
        adUnit.setSize(adUnitRequest.getSize());
        
        return adUnitRepository.save(adUnit);
    }

    @Override
    public void deleteAdUnit(String id) {
        AdUnit adUnit = getAdUnitById(id);
        
        // 从应用中移除广告位ID
        appService.removeAdUnitId(adUnit.getAppId(), id);
        
        adUnitRepository.delete(adUnit);
        log.info("广告位已删除: {}", id);
    }

    @Override
    public List<AdUnit> getAdUnitsByAppId(String appId) {
        // 确认应用存在
        appService.getAppById(appId)
                .orElseThrow(() -> new EntityNotFoundException("应用不存在: " + appId));
        return adUnitRepository.findByAppId(appId);
    }

    @Override
    public List<AdUnit> getAdUnitsByType(String type) {
        return adUnitRepository.findByType(type);
    }

    @Override
    public List<AdUnit> getActiveAdUnits() {
        return adUnitRepository.findByActive(true);
    }

    @Override
    public List<AdUnit> getActiveAdUnitsByAppId(String appId) {
        // 确认应用存在
        appService.getAppById(appId)
                .orElseThrow(() -> new EntityNotFoundException("应用不存在: " + appId));
        return adUnitRepository.findByAppIdAndActive(appId, true);
    }
} 