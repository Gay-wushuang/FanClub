package com.example.controller;

import com.example.pojo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    /**
     * 根路径健康检查
     * @return 健康状态响应
     */
    @GetMapping("/")
    public Result healthCheck() {
        return Result.success("Service is running");
    }
    
    /**
     * API 健康检查
     * @return 健康状态响应
     */
    @GetMapping("/api/v1/")
    public Result apiHealthCheck() {
        return Result.success("API service is running");
    }
}
