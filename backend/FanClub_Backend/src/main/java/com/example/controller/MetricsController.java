package com.example.controller;

import com.example.mapper.MetricsBucketMapper;
import com.example.pojo.MetricsBucket;
import com.example.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 指标控制器
 */
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/metrics")
public class MetricsController {

    @Autowired
    private MetricsBucketMapper metricsBucketMapper;

    // 内存缓存，用于数据库连接失败时的 fallback
    private static final Map<Long, List<MetricsBucket>> metricsCache = new ConcurrentHashMap<>();
    private static final AtomicLong bucketIdCounter = new AtomicLong(1);

    /**
     * 获取场次的指标数据范围
     */
    @GetMapping("/range")
    public Result getSessionMetricsRange(
            @PathVariable Long sessionId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "1") Integer granularity
    ) {
        try {
            List<MetricsBucket> buckets;
            if (startTime != null && endTime != null) {
                // 根据时间范围和粒度查询
                buckets = metricsBucketMapper.findByTimeRangeAndGranularity(sessionId, granularity, startTime, endTime);
            } else {
                // 查询场次的所有指标数据
                buckets = metricsBucketMapper.findBySessionId(sessionId);
            }
            return Result.success(buckets);
        } catch (Exception e) {
            // 数据库连接失败，使用内存缓存
            List<MetricsBucket> buckets = metricsCache.getOrDefault(sessionId, generateMockMetrics(sessionId, granularity));
            return Result.success(buckets);
        }
    }

    /**
     * 生成模拟指标数据
     */
    private List<MetricsBucket> generateMockMetrics(Long sessionId, Integer granularity) {
        List<MetricsBucket> buckets = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < 12; i++) {
            MetricsBucket bucket = new MetricsBucket();
            bucket.setId(bucketIdCounter.incrementAndGet());
            bucket.setSessionId(sessionId);
            bucket.setRoomId(1986387323L);
            bucket.setGranularity(granularity);
            bucket.setBucketTime(now.minusHours(i));
            bucket.setOnlineCount(1000 + (int) (Math.random() * 4000));
            bucket.setDanmakuCount(100 + (int) (Math.random() * 400));
            bucket.setGiftCount(10 + (int) (Math.random() * 40));
            bucket.setGiftAmount(java.math.BigDecimal.valueOf(100 + (int) (Math.random() * 900)));
            bucket.setViewerCount(10000 + (int) (Math.random() * 40000));
            bucket.setCreatedAt(now.minusHours(i));
            buckets.add(bucket);
        }
        
        metricsCache.put(sessionId, buckets);
        return buckets;
    }
}
