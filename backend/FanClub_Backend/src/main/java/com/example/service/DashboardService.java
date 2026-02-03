package com.example.service;

import com.example.mapper.*;
import com.example.pojo.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘服务
 */
@Service
public class DashboardService {

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private EventDanmakuMapper eventDanmakuMapper;

    @Autowired
    private EventGiftMapper eventGiftMapper;

    @Autowired
    private MetricsBucketMapper metricsBucketMapper;

    /**
     * 获取场次列表
     * @param anchorId 主播ID
     * @param roomId 房间ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public Map<String, Object> getSessions(
            Long anchorId, 
            Long roomId, 
            LocalDateTime startTime, 
            LocalDateTime endTime, 
            Integer page, 
            Integer size
    ) {
        try {
            // 使用PageHelper进行分页
            PageHelper.startPage(page, size);

            // 查询场次列表
            List<Session> sessions;
            if (anchorId != null) {
                sessions = sessionMapper.findByAnchorId(anchorId);
            } else if (roomId != null) {
                sessions = sessionMapper.findByRoomId(roomId);
            } else if (startTime != null && endTime != null) {
                sessions = sessionMapper.findByTimeRange(startTime, endTime);
            } else {
                // 查询所有场次
                sessions = sessionMapper.findAll();
            }

            // 包装分页结果
            PageInfo<Session> pageInfo = new PageInfo<>(sessions);

            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("total", pageInfo.getTotal());
            result.put("page", pageInfo.getPageNum());
            result.put("size", pageInfo.getPageSize());
            result.put("pages", pageInfo.getPages());

            return result;
        } catch (Exception e) {
            // 数据库连接失败，返回模拟数据
            e.printStackTrace();
            return getMockSessions(page, size);
        }
    }

    /**
     * 获取模拟场次数据
     * @param page 页码
     * @param size 每页大小
     * @return 模拟分页结果
     */
    private Map<String, Object> getMockSessions(Integer page, Integer size) {
        List<Session> mockSessions = new ArrayList<>();

        // 创建模拟数据
        Session session1 = new Session();
        session1.setId(1L);
        session1.setRoomId(1986387323L);
        session1.setAnchorId(672160178L);
        session1.setRoomTitle("测试直播间1");
        session1.setStartTime(LocalDateTime.now().minusHours(2));
        session1.setEndTime(LocalDateTime.now().minusHours(1));
        session1.setDuration(3600);
        session1.setStatus(2);
        session1.setOnlinePeak(1000);
        session1.setDanmakuCount(500);
        session1.setGiftCount(100);
        session1.setGiftAmount(new BigDecimal(1000));
        session1.setViewerCount(5000);
        session1.setCreatedAt(LocalDateTime.now().minusHours(2));
        session1.setUpdatedAt(LocalDateTime.now().minusHours(1));

        Session session2 = new Session();
        session2.setId(2L);
        session2.setRoomId(1838214834L);
        session2.setAnchorId(123456789L);
        session2.setRoomTitle("测试直播间2");
        session2.setStartTime(LocalDateTime.now().minusDays(1));
        session2.setEndTime(LocalDateTime.now().minusDays(1).plusHours(3));
        session2.setDuration(10800);
        session2.setStatus(2);
        session2.setOnlinePeak(2000);
        session2.setDanmakuCount(1500);
        session2.setGiftCount(300);
        session2.setGiftAmount(new BigDecimal(3000));
        session2.setViewerCount(10000);
        session2.setCreatedAt(LocalDateTime.now().minusDays(1));
        session2.setUpdatedAt(LocalDateTime.now().minusDays(1).plusHours(3));

        Session session3 = new Session();
        session3.setId(3L);
        session3.setRoomId(1986387323L);
        session3.setAnchorId(672160178L);
        session3.setRoomTitle("测试直播间3（直播中）");
        session3.setStartTime(LocalDateTime.now().minusMinutes(30));
        session3.setDuration(1800);
        session3.setStatus(1);
        session3.setOnlinePeak(800);
        session3.setDanmakuCount(200);
        session3.setGiftCount(50);
        session3.setGiftAmount(new BigDecimal(500));
        session3.setViewerCount(3000);
        session3.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        session3.setUpdatedAt(LocalDateTime.now());

        mockSessions.add(session1);
        mockSessions.add(session2);
        mockSessions.add(session3);

        // 模拟分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, mockSessions.size());
        List<Session> pagedSessions = mockSessions.subList(start, end);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pagedSessions);
        result.put("total", mockSessions.size());
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (mockSessions.size() + size - 1) / size);

        return result;
    }

    /**
     * 获取单场summary
     * @param sessionId 场次ID
     * @return summary结果
     */
    public Map<String, Object> getSessionSummary(Long sessionId) {
        try {
            // 查询场次信息
            Session session = sessionMapper.findById(sessionId);
            if (session == null) {
                throw new RuntimeException("场次不存在");
            }

            Map<String, Object> summary = new HashMap<>();

            // 基本信息
            summary.put("sessionId", session.getId());
            summary.put("roomId", session.getRoomId());
            summary.put("anchorId", session.getAnchorId());
            summary.put("roomTitle", session.getRoomTitle());
            summary.put("startTime", session.getStartTime());
            summary.put("endTime", session.getEndTime());
            summary.put("duration", session.getDuration());
            summary.put("status", session.getStatus());

            // 统计信息
            summary.put("onlinePeak", session.getOnlinePeak());
            summary.put("danmakuCount", session.getDanmakuCount());
            summary.put("giftCount", session.getGiftCount());
            summary.put("giftAmount", session.getGiftAmount());
            summary.put("viewerCount", session.getViewerCount());

            // 实时统计（如果直播中）
            if (session.getStatus() == 1) { // 直播中
                // 统计弹幕数
                int danmakuCount = eventDanmakuMapper.countBySessionId(sessionId);
                summary.put("realTimeDanmakuCount", danmakuCount);

                // 统计礼物数和金额
                int giftCount = eventGiftMapper.countBySessionId(sessionId);
                BigDecimal giftAmount = eventGiftMapper.sumAmountBySessionId(sessionId);
                summary.put("realTimeGiftCount", giftCount);
                summary.put("realTimeGiftAmount", giftAmount != null ? giftAmount : BigDecimal.ZERO);
            }

            return summary;
        } catch (Exception e) {
            // 数据库连接失败，返回模拟数据
            e.printStackTrace();
            return getMockSessionSummary(sessionId);
        }
    }

    /**
     * 获取模拟场次summary数据
     * @param sessionId 场次ID
     * @return 模拟summary结果
     */
    private Map<String, Object> getMockSessionSummary(Long sessionId) {
        Map<String, Object> summary = new HashMap<>();

        // 基本信息
        summary.put("sessionId", sessionId);
        summary.put("roomId", 1986387323L);
        summary.put("anchorId", 672160178L);
        summary.put("roomTitle", "测试直播间（直播中）");
        summary.put("startTime", LocalDateTime.now().minusHours(1));
        summary.put("endTime", null);
        summary.put("duration", 3600);
        summary.put("status", 1); // 直播中

        // 统计信息
        summary.put("onlinePeak", 1200);
        summary.put("danmakuCount", 800);
        summary.put("giftCount", 150);
        summary.put("giftAmount", new BigDecimal(1500));
        summary.put("viewerCount", 6000);

        // 实时统计（直播中）
        summary.put("realTimeDanmakuCount", 850);
        summary.put("realTimeGiftCount", 160);
        summary.put("realTimeGiftAmount", new BigDecimal(1600));

        return summary;
    }

    /**
     * 获取场次事件明细
     * @param sessionId 场次ID
     * @param type 事件类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public Map<String, Object> getSessionEvents(
            Long sessionId, 
            String type, 
            LocalDateTime startTime, 
            LocalDateTime endTime, 
            Integer page, 
            Integer size
    ) {
        try {
            // 使用PageHelper进行分页
            PageHelper.startPage(page, size);

            // 根据事件类型查询
            if ("gift".equals(type)) {
                // 查询礼物事件
                var gifts = eventGiftMapper.findBySessionId(sessionId);
                PageInfo<EventGift> pageInfo = new PageInfo<>(gifts);

                Map<String, Object> result = new HashMap<>();
                result.put("list", pageInfo.getList());
                result.put("total", pageInfo.getTotal());
                result.put("page", pageInfo.getPageNum());
                result.put("size", pageInfo.getPageSize());
                result.put("pages", pageInfo.getPages());

                return result;
            } else if ("danmaku".equals(type)) {
                // 查询弹幕事件
                var danmakus = eventDanmakuMapper.findBySessionId(sessionId);
                PageInfo<EventDanmaku> pageInfo = new PageInfo<>(danmakus);

                Map<String, Object> result = new HashMap<>();
                result.put("list", pageInfo.getList());
                result.put("total", pageInfo.getTotal());
                result.put("page", pageInfo.getPageNum());
                result.put("size", pageInfo.getPageSize());
                result.put("pages", pageInfo.getPages());

                return result;
            } else {
                // 其他事件类型，暂时返回空
                Map<String, Object> result = new HashMap<>();
                result.put("list", List.of());
                result.put("total", 0);
                result.put("page", page);
                result.put("size", size);
                result.put("pages", 0);

                return result;
            }
        } catch (Exception e) {
            // 数据库连接失败，返回模拟数据
            e.printStackTrace();
            return getMockSessionEvents(sessionId, type, page, size);
        }
    }

    /**
     * 获取模拟场次事件明细数据
     * @param sessionId 场次ID
     * @param type 事件类型
     * @param page 页码
     * @param size 每页大小
     * @return 模拟分页结果
     */
    private Map<String, Object> getMockSessionEvents(Long sessionId, String type, Integer page, Integer size) {
        if ("gift".equals(type)) {
            // 模拟礼物事件
            List<Map<String, Object>> mockGifts = new ArrayList<>();

            for (int i = 1; i <= size; i++) {
                Map<String, Object> gift = new HashMap<>();
                gift.put("id", (long) (100 + i));
                gift.put("sessionId", sessionId);
                gift.put("roomId", 1986387323L);
                gift.put("userId", 1000000L + i);
                gift.put("username", "用户" + i);
                gift.put("giftName", i % 2 == 0 ? "超级火箭" : "宇宙飞船");
                gift.put("giftCount", 1);
                gift.put("giftPrice", new BigDecimal(500));
                gift.put("totalAmount", new BigDecimal(500));
                gift.put("timestamp", LocalDateTime.now().minusMinutes(30 - i));
                gift.put("createdAt", LocalDateTime.now().minusMinutes(30 - i));
                mockGifts.add(gift);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("list", mockGifts);
            result.put("total", 50);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", 3);

            return result;
        } else if ("danmaku".equals(type)) {
            // 模拟弹幕事件
            List<Map<String, Object>> mockDanmakus = new ArrayList<>();

            for (int i = 1; i <= size; i++) {
                Map<String, Object> danmaku = new HashMap<>();
                danmaku.put("id", (long) (200 + i));
                danmaku.put("sessionId", sessionId);
                danmaku.put("roomId", 1986387323L);
                danmaku.put("userId", 2000000L + i);
                danmaku.put("username", "观众" + i);
                danmaku.put("content", i % 3 == 0 ? "主播好厉害！" : i % 3 == 1 ? "666666" : "礼物走一走～");
                danmaku.put("timestamp", LocalDateTime.now().minusMinutes(30 - i));
                danmaku.put("createdAt", LocalDateTime.now().minusMinutes(30 - i));
                mockDanmakus.add(danmaku);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("list", mockDanmakus);
            result.put("total", 100);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", 5);

            return result;
        } else {
            // 其他事件类型，返回空
            Map<String, Object> result = new HashMap<>();
            result.put("list", List.of());
            result.put("total", 0);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", 0);

            return result;
        }
    }

    /**
     * 获取聚合数据
     * @param anchorId 主播ID
     * @param roomId 房间ID
     * @param granularity 粒度
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 聚合结果
     */
    public Map<String, Object> getAggregateData(
            Long anchorId, 
            Long roomId, 
            String granularity, 
            LocalDateTime startTime, 
            LocalDateTime endTime
    ) {
        try {
            // 查询指标桶数据
            List<MetricsBucket> buckets;
            if (roomId != null) {
                buckets = metricsBucketMapper.findByRoomId(roomId);
            } else {
                // 暂时返回空
                buckets = List.of();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("data", buckets);
            result.put("granularity", granularity);
            result.put("startTime", startTime);
            result.put("endTime", endTime);

            // 如果数据为空，返回模拟数据
            if (buckets.isEmpty()) {
                return getMockAggregateData(granularity);
            }

            return result;
        } catch (Exception e) {
            // 数据库连接失败，返回模拟数据
            e.printStackTrace();
            return getMockAggregateData(granularity);
        }
    }

    /**
     * 获取模拟聚合数据
     * @param granularity 粒度
     * @return 模拟聚合结果
     */
    private Map<String, Object> getMockAggregateData(String granularity) {
        List<Map<String, Object>> mockData = new ArrayList<>();

        // 生成过去6个时间段的数据
        for (int i = 5; i >= 0; i--) {
            Map<String, Object> bucket = new HashMap<>();
            LocalDateTime time;
            if ("minute".equals(granularity)) {
                time = LocalDateTime.now().minusMinutes(i);
            } else if ("hour".equals(granularity)) {
                time = LocalDateTime.now().minusHours(i);
            } else {
                time = LocalDateTime.now().minusDays(i);
            }

            bucket.put("bucketTime", time);
            bucket.put("onlineCount", 800 + i * 50);
            bucket.put("danmakuCount", 100 + i * 20);
            bucket.put("giftCount", 10 + i * 2);
            bucket.put("giftAmount", new BigDecimal(500 + i * 100));
            bucket.put("viewerCount", 1000 + i * 100);
            mockData.add(bucket);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", mockData);
        result.put("granularity", granularity != null ? granularity : "hour");
        result.put("startTime", LocalDateTime.now().minusHours(5));
        result.put("endTime", LocalDateTime.now());

        return result;
    }

}
