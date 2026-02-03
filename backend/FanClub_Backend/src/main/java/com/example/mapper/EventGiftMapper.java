package com.example.mapper;

import com.example.pojo.EventGift;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 礼物事件Mapper接口
 */
@Mapper
public interface EventGiftMapper {

    /**
     * 根据ID查询礼物
     */
    EventGift findById(@Param("id") Long id);

    /**
     * 根据场次ID查询礼物列表
     */
    List<EventGift> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 根据房间ID查询礼物列表
     */
    List<EventGift> findByRoomId(@Param("roomId") Long roomId);

    /**
     * 根据时间范围查询礼物列表
     */
    List<EventGift> findByTimeRange(
            @Param("sessionId") Long sessionId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 新增礼物
     */
    int insert(EventGift eventGift);

    /**
     * 批量新增礼物
     */
    int batchInsert(@Param("gifts") List<EventGift> gifts);

    /**
     * 删除礼物
     */
    int delete(@Param("id") Long id);

    /**
     * 根据场次ID删除礼物
     */
    int deleteBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 统计场次礼物数
     */
    int countBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 统计场次礼物金额
     */
    BigDecimal sumAmountBySessionId(@Param("sessionId") Long sessionId);

}
