package com.example.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 礼物事件实体类
 */
@Data
public class EventGift implements Serializable {

    private Long id; // 礼物ID
    private Long sessionId; // 场次ID
    private Long roomId; // 直播间ID
    private Long userId; // 用户ID
    private String username; // 用户名
    private String giftName; // 礼物名称
    private Integer giftCount; // 礼物数量
    private BigDecimal giftPrice; // 礼物单价
    private BigDecimal totalAmount; // 总金额
    private LocalDateTime timestamp; // 发送时间
    private LocalDateTime createdAt; // 创建时间

}
