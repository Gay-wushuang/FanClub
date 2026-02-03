package com.example.controller;

import com.example.pojo.Result;
import com.example.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取场次列表
     * @param anchorId 主播ID
     * @param roomId 房间ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param size 每页大小
     * @return 响应结果
     */
    @GetMapping("/sessions")
    public Result getSessions(
            @RequestParam(required = false) Long anchorId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        try {
            return Result.success(dashboardService.getSessions(anchorId, roomId, startTime, endTime, page, size));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取场次列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取单场summary
     * @param sessionId 场次ID
     * @return 响应结果
     */
    @GetMapping("/sessions/{sessionId}/summary")
    public Result getSessionSummary(@PathVariable Long sessionId) {
        try {
            return Result.success(dashboardService.getSessionSummary(sessionId));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取场次summary失败：" + e.getMessage());
        }
    }

    /**
     * 获取场次事件明细
     * @param sessionId 场次ID
     * @param type 事件类型：gift|danmaku|pk|enterleave
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param size 每页大小
     * @return 响应结果
     */
    @GetMapping("/sessions/{sessionId}/events")
    public Result getSessionEvents(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        try {
            return Result.success(dashboardService.getSessionEvents(sessionId, type, startTime, endTime, page, size));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取场次事件明细失败：" + e.getMessage());
        }
    }

    /**
     * 获取聚合数据
     * @param anchorId 主播ID
     * @param roomId 房间ID
     * @param granularity 粒度：minute|hour|day
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 响应结果
     */
    @GetMapping("/aggregate")
    public Result getAggregateData(
            @RequestParam(required = false) Long anchorId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String granularity,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime
    ) {
        try {
            return Result.success(dashboardService.getAggregateData(anchorId, roomId, granularity, startTime, endTime));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取聚合数据失败：" + e.getMessage());
        }
    }

}
