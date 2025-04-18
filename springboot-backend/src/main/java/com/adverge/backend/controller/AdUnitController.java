package com.adverge.backend.controller;

import com.adverge.backend.dto.AdUnitRequest;
import com.adverge.backend.model.AdUnit;
import com.adverge.backend.service.AdUnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ad-units")
@RequiredArgsConstructor
public class AdUnitController {

    private final AdUnitService adUnitService;

    /**
     * 获取所有广告位
     */
    @GetMapping
    public ResponseEntity<List<AdUnit>> getAllAdUnits() {
        List<AdUnit> adUnits = adUnitService.getAllAdUnits();
        return ResponseEntity.ok(adUnits);
    }

    /**
     * 根据ID获取广告位
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdUnit> getAdUnitById(@PathVariable String id) {
        AdUnit adUnit = adUnitService.getAdUnitById(id);
        return ResponseEntity.ok(adUnit);
    }

    /**
     * 创建新广告位
     */
    @PostMapping
    public ResponseEntity<AdUnit> createAdUnit(@Valid @RequestBody AdUnitRequest adUnitRequest) {
        AdUnit createdAdUnit = adUnitService.createAdUnit(adUnitRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAdUnit);
    }

    /**
     * 更新广告位信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdUnit> updateAdUnit(@PathVariable String id, @Valid @RequestBody AdUnitRequest adUnitRequest) {
        AdUnit updatedAdUnit = adUnitService.updateAdUnit(id, adUnitRequest);
        return ResponseEntity.ok(updatedAdUnit);
    }

    /**
     * 删除广告位
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdUnit(@PathVariable String id) {
        adUnitService.deleteAdUnit(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据应用ID获取广告位
     */
    @GetMapping("/app/{appId}")
    public ResponseEntity<List<AdUnit>> getAdUnitsByAppId(@PathVariable String appId) {
        List<AdUnit> adUnits = adUnitService.getAdUnitsByAppId(appId);
        return ResponseEntity.ok(adUnits);
    }

    /**
     * 根据类型获取广告位
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<AdUnit>> getAdUnitsByType(@PathVariable String type) {
        List<AdUnit> adUnits = adUnitService.getAdUnitsByType(type);
        return ResponseEntity.ok(adUnits);
    }

    /**
     * 获取活跃广告位
     */
    @GetMapping("/active")
    public ResponseEntity<List<AdUnit>> getActiveAdUnits() {
        List<AdUnit> adUnits = adUnitService.getActiveAdUnits();
        return ResponseEntity.ok(adUnits);
    }

    /**
     * 获取应用的活跃广告位
     */
    @GetMapping("/app/{appId}/active")
    public ResponseEntity<List<AdUnit>> getActiveAdUnitsByAppId(@PathVariable String appId) {
        List<AdUnit> adUnits = adUnitService.getActiveAdUnitsByAppId(appId);
        return ResponseEntity.ok(adUnits);
    }
} 