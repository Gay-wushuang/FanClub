package com.example.mapper;

import com.example.pojo.EventDanmaku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 弹幕事件Mapper接口
 */
@Mapper
public interface EventDanmakuMapper {

    /**
     * 根据ID查询弹幕
     */
    EventDanmaku findById(@Param("id") Long id);

    /**
     * 根据场次ID查询弹幕列表
     */
    List<EventDanmaku> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 根据房间ID查询弹幕列表
     */
    List<EventDanmaku> findByRoomId(@Param("roomId") Long roomId);

    /**
     * 根据时间范围查询弹幕列表
     */
    List<EventDanmaku> findByTimeRange(
            @Param("sessionId") Long sessionId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 新增弹幕
     */
    int insert(EventDanmaku eventDanmaku);

    /**
     * 批量新增弹幕
     */
    int batchInsert(@Param("danmakus") List<EventDanmaku> danmakus);

    /**
     * 删除弹幕
     */
    int delete(@Param("id") Long id);

    /**
     * 根据场次ID删除弹幕
     */
    int deleteBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 统计场次弹幕数
     */
    int countBySessionId(@Param("sessionId") Long sessionId);

}
