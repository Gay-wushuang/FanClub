package com.example.controller;

import com.example.bilibili.service.BilibiliApiService;
import com.example.mapper.SessionMapper;
import com.example.pojo.Result;
import com.example.pojo.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 直播场次控制器
 */
@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private BilibiliApiService bilibiliApiService;

    // 内存缓存，用于数据库连接失败时的 fallback
    private static final Map<Long, Session> sessionCache = new ConcurrentHashMap<>();
    private static long sessionIdCounter = 1;

    /**
     * 确保直播场次存在
     * 逻辑：调用 /room/stats 拿 live_status/live_time/uid
     * 如果 live_status==1：
     * 若当前没有"进行中 session"，则 INSERT sessions (status=LIVE, start_time=live_time, ...)
     * 若有，则更新 title/category/status/updated_at
     * 如果 live_status!=1：
     * 若有 LIVE session，补 end_time 并置 status=ENDED
     */
    @PostMapping("/ensure")
    public Result ensureSession(@RequestParam Long roomId, @RequestParam Long anchorId) {
        try {
            // 调用 B 站 API 获取直播间状态
            Object roomStatsResponse = bilibiliApiService.getRoomStats(roomId);
            if (roomStatsResponse instanceof Map) {
                Map<String, Object> roomStats = (Map<String, Object>) roomStatsResponse;
                Map<String, Object> data = (Map<String, Object>) roomStats.get("data");
                if (data != null) {
                    Integer liveStatus = (Integer) data.get("live_status");
                    String roomTitle = (String) data.get("title");
                    Long uid = ((Number) data.get("uid")).longValue();

                    // 尝试从数据库查询
                    List<Session> liveSessions = null;
                    Session currentSession = null;
                    boolean dbAvailable = true;

                    try {
                        liveSessions = sessionMapper.findLiveSessions();
                        for (Session session : liveSessions) {
                            if (session.getRoomId().equals(roomId)) {
                                currentSession = session;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // 数据库连接失败，使用内存缓存
                        dbAvailable = false;
                        for (Session session : sessionCache.values()) {
                            if (session.getRoomId().equals(roomId) && session.getStatus() == 1) {
                                currentSession = session;
                                break;
                            }
                        }
                    }

                    if (liveStatus == 1) { // 直播中
                        if (currentSession == null) {
                            // 创建新的场次
                            Session newSession = new Session();
                            newSession.setId(sessionIdCounter++);
                            newSession.setRoomId(roomId);
                            newSession.setAnchorId(anchorId);
                            newSession.setPlatformUid(uid);
                            newSession.setRoomTitle(roomTitle);
                            newSession.setStartTime(LocalDateTime.now());
                            newSession.setStatus(1); // 直播中
                            newSession.setCreatedAt(LocalDateTime.now());
                            newSession.setUpdatedAt(LocalDateTime.now());

                            // 尝试保存到数据库
                            if (dbAvailable) {
                                try {
                                    sessionMapper.insert(newSession);
                                } catch (Exception e) {
                                    // 数据库保存失败，使用内存缓存
                                    sessionCache.put(newSession.getId(), newSession);
                                }
                            } else {
                                sessionCache.put(newSession.getId(), newSession);
                            }

                            return Result.success(newSession);
                        } else {
                            // 更新现有场次
                            currentSession.setRoomTitle(roomTitle);
                            currentSession.setStatus(1); // 确保状态为直播中
                            currentSession.setUpdatedAt(LocalDateTime.now());

                            // 尝试更新到数据库
                            if (dbAvailable) {
                                try {
                                    sessionMapper.update(currentSession);
                                } catch (Exception e) {
                                    // 数据库更新失败，使用内存缓存
                                    sessionCache.put(currentSession.getId(), currentSession);
                                }
                            } else {
                                sessionCache.put(currentSession.getId(), currentSession);
                            }

                            return Result.success(currentSession);
                        }
                    } else { // 未开播或轮播
                        if (currentSession != null) {
                            // 结束直播场次
                            currentSession.setEndTime(LocalDateTime.now());
                            currentSession.setDuration((int) (currentSession.getEndTime().getSecond() - currentSession.getStartTime().getSecond()));
                            currentSession.setStatus(2); // 已结束
                            currentSession.setUpdatedAt(LocalDateTime.now());

                            // 尝试更新到数据库
                            if (dbAvailable) {
                                try {
                                    sessionMapper.update(currentSession);
                                } catch (Exception e) {
                                    // 数据库更新失败，使用内存缓存
                                    sessionCache.put(currentSession.getId(), currentSession);
                                }
                            } else {
                                sessionCache.put(currentSession.getId(), currentSession);
                            }

                            return Result.success(currentSession);
                        } else {
                            return Result.success();
                        }
                    }
                }
            }
            return Result.error("获取直播间状态失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("系统内部错误: " + e.getMessage());
        }
    }

    /**
     * 获取场次列表
     */
    @GetMapping
    public Result getSessions() {
        try {
            List<Session> sessions = sessionMapper.findAll();
            return Result.success(sessions);
        } catch (Exception e) {
            // 数据库连接失败，使用内存缓存
            List<Session> sessions = new ArrayList<>(sessionCache.values());
            return Result.success(sessions);
        }
    }

    /**
     * 获取直播中的场次
     */
    @GetMapping("/live")
    public Result getLiveSessions() {
        try {
            List<Session> liveSessions = sessionMapper.findLiveSessions();
            return Result.success(liveSessions);
        } catch (Exception e) {
            // 数据库连接失败，使用内存缓存
            List<Session> liveSessions = new ArrayList<>();
            for (Session session : sessionCache.values()) {
                if (session.getStatus() == 1) {
                    liveSessions.add(session);
                }
            }
            return Result.success(liveSessions);
        }
    }

    /**
     * 根据ID获取场次详情
     */
    @GetMapping("/{id}")
    public Result getSessionById(@PathVariable Long id) {
        try {
            Session session = sessionMapper.findById(id);
            if (session != null) {
                return Result.success(session);
            } else {
                // 数据库中不存在，尝试从内存缓存中获取
                Session cachedSession = sessionCache.get(id);
                if (cachedSession != null) {
                    return Result.success(cachedSession);
                } else {
                    return Result.error("场次不存在");
                }
            }
        } catch (Exception e) {
            // 数据库连接失败，尝试从内存缓存中获取
            Session cachedSession = sessionCache.get(id);
            if (cachedSession != null) {
                return Result.success(cachedSession);
            } else {
                return Result.error("获取场次详情失败: " + e.getMessage());
            }
        }
    }
}
