package com.example.filter;

import com.example.utils.CurrentHolder;
import com.example.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@WebFilter("/*")//过滤所有请求
public class TokenFilter implements Filter {
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 设置响应头
        response.setContentType("application/json;charset=UTF-8");

        //1,获取请求路径
        String uri = request.getRequestURI();
        log.info("请求路径: {}", uri);

        //2,排除不需要验证token的请求路径
        if (uri.contains("/login") || uri.contains("/register") || uri.contains("/bilibili") || 
            uri.contains("/health") || uri.contains("/api/v1/auth") || 
            uri.contains("/v3/api-docs") || uri.contains("/swagger-ui")) {

            log.info("无需验证token，直接放行: {}", uri);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        //3,获取请求头中的token
        String token = request.getHeader("token");

        //4,验证token
        if (token == null || token.isEmpty()) {
            log.info("令牌为空，响应401");
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\":40001,\"msg\":\"未授权访问\",\"data\":null}");
            writer.flush();
            writer.close();
            return;
        }

        //5,如果token存在,校验令牌
        try {
            Claims claims = JwtUtils.parseToken(token);

            // 从claims中获取用户信息
            Long userId = Long.valueOf(claims.get("userId").toString());
            Integer roleId = Integer.valueOf(claims.get("roleId").toString());
            String username = claims.get("username").toString();
            String roleName = claims.get("roleName").toString();

            // 设置当前用户信息到线程变量
            CurrentHolder.setUserId(userId);
            CurrentHolder.setUsername(username);
            CurrentHolder.setRoleId(roleId);

            log.info("当前登录用户: userId={}, username={}, roleId={}, roleName={}", userId, username, roleId, roleName);

        } catch (Exception e) {
            log.error("令牌验证失败，响应401: {}", e.getMessage());
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\":40001,\"msg\":\"令牌验证失败\",\"data\":null}");
            writer.flush();
            writer.close();
            return;
        }

        //6,放行请求
        log.info("令牌验证成功，放行请求: {}", uri);
        filterChain.doFilter(servletRequest, servletResponse);

        //7,清理线程变量
        CurrentHolder.remove();
    }
}

