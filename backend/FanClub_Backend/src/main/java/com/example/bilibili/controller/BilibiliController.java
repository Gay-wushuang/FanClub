package com.example.bilibili.controller;

import com.example.bilibili.pojo.BilibiliRoomInitResponse;
import com.example.bilibili.service.BilibiliApiService;
import com.example.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/bilibili")
public class BilibiliController {
    
    @Autowired
    private BilibiliApiService bilibiliApiService;
    
    /**
     * 根据短号获取真实的房间ID
     * @param shortId 直播间短号
     * @return 响应结果
     */
    @GetMapping("/room/init")
    public Result getRoomInit(@RequestParam long shortId) {
        try {
            Object response = bilibiliApiService.getRoomInit(shortId);
            return Result.success(response);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("获取房间信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取直播间信息
     * @param roomId 真实房间ID
     * @return 响应结果
     */
    @GetMapping("/room/info")
    public Result getRoomInfo(@RequestParam long roomId) {
        try {
            Object response = bilibiliApiService.getRoomInfo(roomId);
            return Result.success(response);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("获取直播间信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取直播间统计数据
     * @param roomId 真实房间ID
     * @return 响应结果
     */
    @GetMapping("/room/stats")
    public Result getRoomStats(@RequestParam long roomId) {
        try {
            Object response = bilibiliApiService.getRoomStats(roomId);
            return Result.success(response);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("获取直播间统计数据失败：" + e.getMessage());
        }
    }
}
