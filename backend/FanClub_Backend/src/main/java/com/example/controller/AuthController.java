package com.example.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.UserInfoResponse;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.utils.PasswordUtil;
import com.example.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、登出、刷新令牌等接口")
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());

        // 1. 验证用户是否存在
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
        if (userOptional.isEmpty()) {
            log.warn("登录失败: 用户不存在 - {}", request.getUsername());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "用户名或密码错误"));
        }

        User user = userOptional.get();

        // 2. 验证账户是否启用
        if (user.getStatus() == 0) {
            log.warn("登录失败: 账户已禁用 - {}", request.getUsername());
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "账户已被禁用，请联系管理员"));
        }

        // 3. 验证密码
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败: 密码错误 - {}", request.getUsername());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "用户名或密码错误"));
        }

        // 4. SA-Token 登录
        try {
            // 使用用户ID进行登录，SA-Token会自动生成token并写入Cookie
            StpUtil.login(user.getId());

            // 获取登录信息
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

            // 转换角色为字符串集合
            Set<String> roleCodes = user.getRoles().stream()
                    .map(Role::getRoleCode)
                    .collect(Collectors.toSet());

            // 5. 构建响应
            LoginResponse response = LoginResponse.builder()
                    .accessToken(tokenInfo.getTokenValue())
                    .refreshToken(tokenInfo.getTokenValue()) // SA-Token默认没有单独的refresh token
                    .expiresInSec(tokenInfo.getTokenTimeout())
                    .roles(roleCodes)
                    .userId(user.getId().toString())
                    .nickname(user.getNickname())
                    .anchorIds(user.getAnchorIds())
                    .roomIds(user.getRoomIds())
                    .build();

            log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("登录异常: username={}", request.getUsername(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "登录系统异常"));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "刷新访问令牌")
    public ResponseEntity<ApiResponse<Object>> refreshToken() {
        // SA-Token 会自动续期，此接口可以返回新的token或直接成功
        StpUtil.checkLogin();

        // 如果需要返回新的token
        SaTokenInfo newTokenInfo = StpUtil.getTokenInfo();

        LoginResponse response = LoginResponse.builder()
                .accessToken(newTokenInfo.getTokenValue())
                .refreshToken(newTokenInfo.getTokenValue())
                .expiresInSec(newTokenInfo.getTokenTimeout())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "用户退出登录")
    public ResponseEntity<ApiResponse<Object>> logout() {
        StpUtil.logout();
        return ResponseEntity.ok(ApiResponse.success("退出成功"));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取已登录用户的详细信息")
    public ResponseEntity<ApiResponse<Object>> getCurrentUser() {
        StpUtil.checkLogin();

        Long userId = StpUtil.getLoginIdAsLong();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 转换角色为字符串集合
        Set<String> roleCodes = user.getRoles().stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toSet());

        UserInfoResponse response = UserInfoResponse.builder()
                .userId(user.getId().toString())
                .nickname(user.getNickname())
                .roles(roleCodes)
                .anchorIds(user.getAnchorIds())
                .roomIds(user.getRoomIds())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}