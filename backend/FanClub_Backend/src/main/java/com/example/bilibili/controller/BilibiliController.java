package com.example.bilibili.controller;

import com.example.bilibili.pojo.BilibiliRoomInitResponse;
import com.example.bilibili.service.BilibiliApiService;
import com.example.pojo.Result;
import com.example.utils.RoomIdParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/bilibili")
public class BilibiliController {
    
    @Autowired
    private BilibiliApiService bilibiliApiService;
    
    /**
     * 根据ID获取真实的房间信息
     * @param request HTTP请求对象
     * @return 响应结果
     */
    @GetMapping("/room/init")
    public Result getRoomInit(HttpServletRequest request) {
        try {
            // 使用统一的参数解析工具
            Long roomId = RoomIdParser.parseRoomId(request);
            Object response = bilibiliApiService.getRoomInit(roomId);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            // 参数错误，返回4xx错误
            return Result.error(400, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("获取房间信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取直播间信息
     * @param request HTTP请求对象
     * @return 响应结果
     */
    @GetMapping("/room/info")
    public Result getRoomInfo(HttpServletRequest request) {
        try {
            // 使用统一的参数解析工具
            Long roomId = RoomIdParser.parseRoomId(request);
            Object response = bilibiliApiService.getRoomInfo(roomId);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            // 参数错误，返回4xx错误
            return Result.error(400, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("获取直播间信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取直播间统计数据
     * @param request HTTP请求对象
     * @return 响应结果
     */
    @GetMapping("/room/stats")
    public Result getRoomStats(HttpServletRequest request) {
        try {
            // 使用统一的参数解析工具
            Long roomId = RoomIdParser.parseRoomId(request);
            Object response = bilibiliApiService.getRoomStats(roomId);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            // 参数错误，返回4xx错误
            return Result.error(400, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("获取直播间统计数据失败：" + e.getMessage());
        }
    }
}
