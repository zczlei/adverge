package com.adverge.backend.controller;

import com.adverge.backend.model.Metrics;
import com.adverge.backend.repository.MetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatController {

    private final MetricsRepository metricsRepository;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 获取平台收益统计
     */
    @GetMapping("/platform")
    public ResponseEntity<List<Map<String, Object>>> getPlatformStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        
        if (startDate == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            startDate = cal.getTime();
        }
        
        if (endDate == null) {
            endDate = new Date();
        }
        
        // 从数据库查询指定时间范围内的数据
        List<Metrics> metrics = metricsRepository.findByTimestampBetween(startDate, endDate);
        
        // 按平台分组统计
        Map<String, DoubleSummaryStatistics> stats = metrics.stream()
                .collect(Collectors.groupingBy(
                        Metrics::getPlatform,
                        Collectors.summarizingDouble(Metrics::getPrice)
                ));
        
        // 转换为前端所需格式
        List<Map<String, Object>> result = new ArrayList<>();
        stats.forEach((platform, stat) -> {
            Map<String, Object> platformStat = new HashMap<>();
            platformStat.put("platform", platform);
            platformStat.put("revenue", stat.getSum());
            platformStat.put("count", stat.getCount());
            platformStat.put("average", stat.getAverage());
            result.add(platformStat);
        });
        
        // 按收益排序
        result.sort((a, b) -> Double.compare((Double) b.get("revenue"), (Double) a.get("revenue")));
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取广告位收益统计
     */
    @GetMapping("/placement")
    public ResponseEntity<List<Map<String, Object>>> getPlacementStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        
        if (startDate == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            startDate = cal.getTime();
        }
        
        if (endDate == null) {
            endDate = new Date();
        }
        
        // 从数据库查询指定时间范围内的数据
        List<Metrics> metrics = metricsRepository.findByTimestampBetween(startDate, endDate);
        
        // 按广告位分组统计
        Map<String, DoubleSummaryStatistics> stats = metrics.stream()
                .collect(Collectors.groupingBy(
                        Metrics::getPlacementId,
                        Collectors.summarizingDouble(Metrics::getPrice)
                ));
        
        // 转换为前端所需格式
        List<Map<String, Object>> result = new ArrayList<>();
        stats.forEach((placementId, stat) -> {
            Map<String, Object> placementStat = new HashMap<>();
            placementStat.put("placementId", placementId);
            placementStat.put("revenue", stat.getSum());
            placementStat.put("count", stat.getCount());
            placementStat.put("average", stat.getAverage());
            result.add(placementStat);
        });
        
        // 按收益排序
        result.sort((a, b) -> Double.compare((Double) b.get("revenue"), (Double) a.get("revenue")));
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取每日收益统计
     */
    @GetMapping("/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        
        if (startDate == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            startDate = cal.getTime();
        }
        
        if (endDate == null) {
            endDate = new Date();
        }
        
        // 从数据库查询指定时间范围内的数据
        List<Metrics> metrics = metricsRepository.findByTimestampBetween(startDate, endDate);
        
        // 按日期分组统计
        Map<String, DoubleSummaryStatistics> stats = metrics.stream()
                .collect(Collectors.groupingBy(
                        m -> {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(m.getTimestamp());
                            return String.format("%d-%02d-%02d", 
                                    cal.get(Calendar.YEAR), 
                                    cal.get(Calendar.MONTH) + 1, 
                                    cal.get(Calendar.DAY_OF_MONTH));
                        },
                        Collectors.summarizingDouble(Metrics::getPrice)
                ));
        
        // 转换为前端所需格式
        List<Map<String, Object>> result = new ArrayList<>();
        stats.forEach((date, stat) -> {
            Map<String, Object> dateStat = new HashMap<>();
            dateStat.put("date", date);
            dateStat.put("revenue", stat.getSum());
            dateStat.put("count", stat.getCount());
            dateStat.put("average", stat.getAverage());
            result.add(dateStat);
        });
        
        // 按日期排序
        result.sort(Comparator.comparing(m -> (String) m.get("date")));
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取Redis中的平台eCPM排名
     */
    @GetMapping("/ecpm")
    public ResponseEntity<List<Map<String, Object>>> getPlatformECPM() {
        Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> platformsWithScores =
                redisTemplate.opsForZSet().reverseRangeWithScores("platform:ecpm", 0, -1);
        
        List<Map<String, Object>> result = new ArrayList<>();
        if (platformsWithScores != null) {
            int rank = 1;
            for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<String> tuple : platformsWithScores) {
                Map<String, Object> platformECPM = new HashMap<>();
                platformECPM.put("rank", rank++);
                platformECPM.put("platform", tuple.getValue());
                platformECPM.put("ecpm", tuple.getScore());
                result.add(platformECPM);
            }
        }
        
        return ResponseEntity.ok(result);
    }
} 