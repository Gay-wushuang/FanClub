package com.example.mapper;

import com.example.pojo.MetricsBucket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 指标桶Mapper接口
 */
@Mapper
public interface MetricsBucketMapper {

    /**
     * 根据ID查询指标桶
     */
    MetricsBucket findById(@Param("id") Long id);

    /**
     * 根据场次ID查询指标桶列表
     */
    List<MetricsBucket> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 根据房间ID查询指标桶列表
     */
    List<MetricsBucket> findByRoomId(@Param("roomId") Long roomId);

    /**
     * 根据粒度查询指标桶列表
     */
    List<MetricsBucket> findByGranularity(
            @Param("sessionId") Long sessionId,
            @Param("granularity") Integer granularity
    );

    /**
     * 根据时间范围和粒度查询指标桶列表
     */
    List<MetricsBucket> findByTimeRangeAndGranularity(
            @Param("sessionId") Long sessionId,
            @Param("granularity") Integer granularity,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 新增指标桶
     */
    int insert(MetricsBucket metricsBucket);

    /**
     * 批量新增指标桶
     */
    int batchInsert(@Param("buckets") List<MetricsBucket> buckets);

    /**
     * 更新指标桶
     */
    int update(MetricsBucket metricsBucket);

    /**
     * 删除指标桶
     */
    int delete(@Param("id") Long id);

    /**
     * 根据场次ID删除指标桶
     */
    int deleteBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 根据房间ID删除指标桶
     */
    int deleteByRoomId(@Param("roomId") Long roomId);

}
