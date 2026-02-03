package com.example.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 直播场次实体类
 */
@Data
public class Session implements Serializable {

    private Long id; // 场次ID
    private Long roomId; // 直播间ID
    private Long anchorId; // 主播ID
    private Long platformUid; // 平台用户ID
    private String gameId; // open-live game_id
    private String openliveStatus; // NONE/STARTED/ENDED
    private String roomTitle; // 直播间标题
    private LocalDateTime startTime; // 开播时间
    private LocalDateTime endTime; // 结束时间
    private Integer duration; // 时长（秒）
    private Integer status; // 状态：0-未开始，1-直播中，2-已结束
    private Integer onlinePeak; // 在线峰值
    private Integer danmakuCount; // 弹幕数
    private Integer giftCount; // 礼物数
    private BigDecimal giftAmount; // 礼物金额
    private Integer viewerCount; // 观看人数
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间

}
