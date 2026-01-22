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

@Slf4j
@WebFilter("/*")//过滤所有请求
public class TokenFilter implements Filter {
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1,获取请求路径
        String uri = request.getRequestURI();
        //2,排除不需要验证token的请求路径
        if (uri.contains("/login") || uri.contains("/register")) {

            log.info("无需验证token，直接放行: {}", uri);

            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        //3,获取请求头中的token
        String token = request.getHeader("token");

        //4,验证token
        if (token == null || token.isEmpty()) {
            log.info("令牌为空，响应401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //5,如果token存在,校验令牌
        try {
            Claims claims = JwtUtils.parseToken(token);

            Integer empId = Integer.valueOf(claims.get("id").toString());
            CurrentHolder.setCurrentId(empId);
            log.info("当前登录员工id: {}", empId);

        } catch (Exception e) {
            log.error("令牌验证失败，响应401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //6,放行请求
        log.info("令牌验证成功，放行请求: {}", uri);
        filterChain.doFilter(servletRequest, servletResponse);


        //7,清理线程变量
        CurrentHolder.remove();
    }
}

