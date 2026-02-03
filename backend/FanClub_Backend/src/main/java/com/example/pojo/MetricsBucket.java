package com.example.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 指标桶实体类
 */
@Data
public class MetricsBucket implements Serializable {

    private Long id; // 指标ID
    private Long sessionId; // 场次ID
    private Long roomId; // 直播间ID
    private Integer granularity; // 粒度：1-分钟，2-小时，3-天
    private LocalDateTime bucketTime; // 时间段
    private Integer onlineCount; // 在线人数
    private Integer danmakuCount; // 弹幕数
    private Integer giftCount; // 礼物数
    private BigDecimal giftAmount; // 礼物金额
    private Integer viewerCount; // 观看人数
    private LocalDateTime createdAt; // 创建时间

}
