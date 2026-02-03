package com.example.controller;

import com.example.pojo.Result;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@RequestParam String username, @RequestParam String password) {
        try {
            Map<String, Object> result = userService.login(username, password);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @PostMapping("/info")
    public Result getInfo() {
        // 从TokenFilter中获取当前用户ID
        Long userId = com.example.utils.CurrentHolder.getUserId();
        if (userId == null) {
            return Result.unauthorized("未登录");
        }

        try {
            return Result.success(userService.findById(userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result logout() {
        // 实际项目中可能需要处理token黑名单
        return Result.success();
    }

}
