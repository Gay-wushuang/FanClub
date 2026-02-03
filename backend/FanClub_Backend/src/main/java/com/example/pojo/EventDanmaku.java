package com.example.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 弹幕事件实体类
 */
@Data
public class EventDanmaku implements Serializable {

    private Long id; // 弹幕ID
    private Long sessionId; // 场次ID
    private Long roomId; // 直播间ID
    private Long userId; // 用户ID
    private String username; // 用户名
    private String content; // 弹幕内容
    private LocalDateTime timestamp; // 发送时间
    private LocalDateTime createdAt; // 创建时间

}
