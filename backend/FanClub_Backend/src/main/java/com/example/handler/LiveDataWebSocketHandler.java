package com.example.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.example.bilibili.service.BilibiliApiService;
import com.example.pojo.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LiveDataWebSocketHandler extends TextWebSocketHandler {

    // 存储所有活跃的WebSocket会话
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    // 存储每个会话订阅的房间ID
    private static final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();
    
    // 定时器，用于定时推送数据
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
    
    // Bilibili API服务
    private final BilibiliApiService bilibiliApiService = new BilibiliApiService();
    
    // JSON序列化工具
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public LiveDataWebSocketHandler() {
        // 启动定时任务，每5秒推送一次数据
        executorService.scheduleAtFixedRate(this::pushLiveData, 0, 5, TimeUnit.SECONDS);
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 新连接建立时
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        System.out.println("WebSocket连接建立: " + sessionId);
        
        // 发送连接成功消息
        Result result = Result.success("WebSocket连接成功");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端发送的消息
        String sessionId = session.getId();
        String payload = message.getPayload();
        
        try {
            // 解析客户端消息，格式: {"id": "123456"}
            Map<String, String> data = objectMapper.readValue(payload, Map.class);
            String roomId = data.get("id");
            
            if (roomId != null) {
                // 存储房间ID订阅关系
                sessionRoomMap.put(sessionId, roomId);
                System.out.println("会话 " + sessionId + " 订阅了房间: " + roomId);
                
                // 发送订阅成功消息
                Result result = Result.success("订阅房间 " + roomId + " 成功");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
            } else {
                // 发送错误消息
                Result result = Result.error("缺少id参数");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
            }
        } catch (Exception e) {
            // 发送错误消息
            Result result = Result.error("消息格式错误");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 连接关闭时
        String sessionId = session.getId();
        sessions.remove(sessionId);
        sessionRoomMap.remove(sessionId);
        System.out.println("WebSocket连接关闭: " + sessionId);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 处理传输错误
        String sessionId = session.getId();
        System.out.println("WebSocket传输错误: " + sessionId + ", 错误: " + exception.getMessage());
    }
    
    // 推送直播数据
    private void pushLiveData() {
        // 遍历所有活跃会话
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            String sessionId = entry.getKey();
            WebSocketSession session = entry.getValue();
            String roomId = sessionRoomMap.get(sessionId);
            
            if (session.isOpen() && roomId != null) {
                try {
                    // 获取房间状态信息
                    Object roomStats = bilibiliApiService.getRoomStats(Long.parseLong(roomId));
                    
                    // 构建响应
                    Result result = Result.success(roomStats);
                    String message = objectMapper.writeValueAsString(result);
                    
                    // 发送消息
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    System.out.println("推送数据失败: " + sessionId + ", 错误: " + e.getMessage());
                    // 可以在这里处理错误，比如发送错误消息给客户端
                }
            }
        }
    }
    
    // 关闭定时器
    public void shutdown() {
        executorService.shutdown();
    }
}
