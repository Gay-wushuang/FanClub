package com.example.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 房间ID参数解析工具类
 * 统一处理房间ID参数的解析，只支持新参数名id
 */
public class RoomIdParser {
    
    /**
     * 解析房间ID参数
     * @param request HTTP请求对象
     * @return 解析得到的房间ID
     * @throws IllegalArgumentException 如果没有提供有效的房间ID参数
     */
    public static Long parseRoomId(HttpServletRequest request) {
        // 只使用新参数名id
        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            try {
                return Long.parseLong(idParam);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("无效的房间ID参数: " + idParam);
            }
        }
        
        // 检查是否使用了旧参数名
        String shortIdParam = request.getParameter("shortId");
        String roomIdParam = request.getParameter("roomId");
        
        if (shortIdParam != null && !shortIdParam.isEmpty()) {
            throw new IllegalArgumentException("参数名错误: 请使用'id'参数，不再支持'shortId'参数");
        } else if (roomIdParam != null && !roomIdParam.isEmpty()) {
            throw new IllegalArgumentException("参数名错误: 请使用'id'参数，不再支持'roomId'参数");
        }
        
        // 如果没有提供任何有效的房间ID参数
        throw new IllegalArgumentException("缺少房间ID参数，请使用'id'参数");
    }
    
    /**
     * 解析直播房间ID参数（针对room/init端点）
     * @param request HTTP请求对象
     * @return 解析得到的房间ID
     * @throws IllegalArgumentException 如果没有提供有效的房间ID参数
     */
    public static Long parseInitRoomId(HttpServletRequest request) {
        return parseRoomId(request);
    }
    
    /**
     * 解析直播房间ID参数（针对room/info和room/stats端点）
     * @param request HTTP请求对象
     * @return 解析得到的房间ID
     * @throws IllegalArgumentException 如果没有提供有效的房间ID参数
     */
    public static Long parseStatsRoomId(HttpServletRequest request) {
        return parseRoomId(request);
    }
}
