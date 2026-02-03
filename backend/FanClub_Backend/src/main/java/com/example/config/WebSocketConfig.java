package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.example.handler.LiveDataWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器，路径为/api/v1/ws/live-data
        registry.addHandler(new LiveDataWebSocketHandler(), "/api/v1/ws/live-data")
                .setAllowedOrigins("*"); // 允许所有来源，生产环境应该设置具体的域名
    }
}
