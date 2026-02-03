package com.example.mapper;

import com.example.pojo.Session;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 直播场次Mapper接口
 */
@Mapper
public interface SessionMapper {

    /**
     * 根据ID查询场次
     */
    Session findById(@Param("id") Long id);

    /**
     * 根据房间ID查询场次列表
     */
    List<Session> findByRoomId(@Param("roomId") Long roomId);

    /**
     * 根据主播ID查询场次列表
     */
    List<Session> findByAnchorId(@Param("anchorId") Long anchorId);

    /**
     * 根据时间范围查询场次列表
     */
    List<Session> findByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询直播中的场次
     */
    @Select("SELECT * FROM sessions WHERE status = 1 ORDER BY start_time DESC")
    List<Session> findLiveSessions();

    /**
     * 查询所有场次
     */
    @Select("SELECT * FROM sessions ORDER BY start_time DESC")
    List<Session> findAll();

    /**
     * 新增场次
     */
    int insert(Session session);

    /**
     * 更新场次
     */
    int update(Session session);

    /**
     * 删除场次
     */
    int delete(@Param("id") Long id);

    /**
     * 更新场次状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新场次统计数据
     */
    int updateStats(
            @Param("id") Long id,
            @Param("onlinePeak") Integer onlinePeak,
            @Param("danmakuCount") Integer danmakuCount,
            @Param("giftCount") Integer giftCount,
            @Param("giftAmount") java.math.BigDecimal giftAmount,
            @Param("viewerCount") Integer viewerCount
    );

}
