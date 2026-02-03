package com.example.task;

import com.example.bilibili.service.BilibiliApiService;
import com.example.mapper.EventDanmakuMapper;
import com.example.mapper.EventGiftMapper;
import com.example.mapper.MetricsBucketMapper;
import com.example.mapper.SessionMapper;
import com.example.pojo.EventDanmaku;
import com.example.pojo.EventGift;
import com.example.pojo.MetricsBucket;
import com.example.pojo.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据采集定时任务
 */
@Slf4j
@Component
public class DataCollectionTask {

    @Autowired
    private BilibiliApiService bilibiliApiService;

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private EventDanmakuMapper eventDanmakuMapper;

    @Autowired
    private EventGiftMapper eventGiftMapper;

    @Autowired
    private MetricsBucketMapper metricsBucketMapper;

    // 监控的直播间列表
    private final List<Long> monitoredRoomIds = List.of(1986387323L, 1838214834L);

    /**
     * 每10秒采集一次直播数据
     */
    @Scheduled(fixedRate = 10000)
    public void collectLiveData() {
        log.info("开始采集直播数据...");

        for (Long roomId : monitoredRoomIds) {
            try {
                // 获取直播间信息
                Object roomInfoResponse = bilibiliApiService.getRoomInfo(roomId);
                if (roomInfoResponse instanceof Map) {
                    Map<String, Object> roomInfo = (Map<String, Object>) roomInfoResponse;
                    Map<String, Object> data = (Map<String, Object>) roomInfo.get("data");
                    if (data != null) {
                        Integer liveStatus = (Integer) data.get("live_status");
                        String roomTitle = (String) data.get("title");
                        Long uid = ((Number) data.get("uid")).longValue();

                        // 处理直播状态
                        handleLiveStatus(roomId, uid, roomTitle, liveStatus);
                    }
                }

                // 获取直播间统计数据
                Object roomStatsResponse = bilibiliApiService.getRoomStats(roomId);
                if (roomStatsResponse instanceof Map) {
                    Map<String, Object> roomStats = (Map<String, Object>) roomStatsResponse;
                    Map<String, Object> data = (Map<String, Object>) roomStats.get("data");
                    if (data != null) {
                        Integer onlineCount = (Integer) data.get("online");
                        // 这里可以添加更多统计数据的处理

                        // 保存指标数据
                        saveMetricsData(roomId, onlineCount);
                    }
                }

            } catch (Exception e) {
                log.error("采集直播间 {} 数据失败: {}", roomId, e.getMessage(), e);
            }
        }

        log.info("直播数据采集完成");
    }

    /**
     * 处理直播状态
     * @param roomId 房间ID
     * @param uid 主播ID
     * @param roomTitle 房间标题
     * @param liveStatus 直播状态：0-未开播，1-直播中，2-轮播
     */
    private void handleLiveStatus(Long roomId, Long uid, String roomTitle, Integer liveStatus) {
        try {
            // 查询是否存在直播中的场次
            List<Session> liveSessions = sessionMapper.findLiveSessions();
            Session currentSession = null;

            for (Session session : liveSessions) {
                if (session.getRoomId().equals(roomId)) {
                    currentSession = session;
                    break;
                }
            }

            if (liveStatus == 1) { // 直播中
                if (currentSession == null) {
                    // 创建新的场次
                    Session newSession = new Session();
                    newSession.setRoomId(roomId);
                    newSession.setAnchorId(uid);
                    newSession.setRoomTitle(roomTitle);
                    newSession.setStartTime(LocalDateTime.now());
                    newSession.setStatus(1); // 直播中
                    newSession.setCreatedAt(LocalDateTime.now());
                    newSession.setUpdatedAt(LocalDateTime.now());

                    sessionMapper.insert(newSession);
                    log.info("创建新的直播场次: 房间ID={}, 标题={}", roomId, roomTitle);
                } else {
                    // 更新现有场次
                    currentSession.setUpdatedAt(LocalDateTime.now());
                    sessionMapper.update(currentSession);
                }
            } else if (liveStatus == 0 || liveStatus == 2) { // 未开播或轮播
                if (currentSession != null) {
                    // 结束直播场次
                    currentSession.setEndTime(LocalDateTime.now());
                    currentSession.setDuration((int) (currentSession.getEndTime().getSecond() - currentSession.getStartTime().getSecond()));
                    currentSession.setStatus(2); // 已结束
                    currentSession.setUpdatedAt(LocalDateTime.now());

                    sessionMapper.update(currentSession);
                    log.info("结束直播场次: 房间ID={}, 标题={}", roomId, currentSession.getRoomTitle());
                }
            }

        } catch (Exception e) {
            log.error("处理直播状态失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 保存指标数据
     * @param roomId 房间ID
     * @param onlineCount 在线人数
     */
    private void saveMetricsData(Long roomId, Integer onlineCount) {
        try {
            // 查询直播中的场次
            List<Session> liveSessions = sessionMapper.findLiveSessions();
            for (Session session : liveSessions) {
                if (session.getRoomId().equals(roomId)) {
                    // 创建指标桶数据
                    MetricsBucket bucket = new MetricsBucket();
                    bucket.setSessionId(session.getId());
                    bucket.setRoomId(roomId);
                    bucket.setGranularity(2); // 小时粒度
                    bucket.setBucketTime(LocalDateTime.now().withMinute(0).withSecond(0).withNano(0));
                    bucket.setOnlineCount(onlineCount);
                    bucket.setDanmakuCount(0); // 暂时设为0，后续可以从弹幕事件中统计
                    bucket.setGiftCount(0); // 暂时设为0，后续可以从礼物事件中统计
                    bucket.setGiftAmount(BigDecimal.ZERO); // 暂时设为0，后续可以从礼物事件中统计
                    bucket.setViewerCount(onlineCount); // 暂时用在线人数代替
                    bucket.setCreatedAt(LocalDateTime.now());

                    // 这里可以添加逻辑来判断是否已经存在相同时间桶的数据
                    // 如果存在，可以更新；如果不存在，可以插入

                    // 暂时直接插入
                    metricsBucketMapper.insert(bucket);
                    log.info("保存指标数据: 房间ID={}, 在线人数={}", roomId, onlineCount);
                    break;
                }
            }

        } catch (Exception e) {
            log.error("保存指标数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每小时清理一次过期数据
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiredData() {
        log.info("开始清理过期数据...");

        try {
            // 这里可以添加清理过期数据的逻辑
            // 例如：删除30天前的事件数据

            log.info("过期数据清理完成");
        } catch (Exception e) {
            log.error("清理过期数据失败: {}", e.getMessage(), e);
        }
    }

}
