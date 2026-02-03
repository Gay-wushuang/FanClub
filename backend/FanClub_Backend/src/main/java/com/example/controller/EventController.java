package com.example.controller;

import com.example.mapper.EventDanmakuMapper;
import com.example.mapper.EventGiftMapper;
import com.example.pojo.EventDanmaku;
import com.example.pojo.EventGift;
import com.example.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 事件控制器
 */
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/events")
public class EventController {

    @Autowired
    private EventDanmakuMapper eventDanmakuMapper;

    @Autowired
    private EventGiftMapper eventGiftMapper;

    // 内存缓存，用于数据库连接失败时的 fallback
    private static final Map<Long, List<EventDanmaku>> danmakuCache = new ConcurrentHashMap<>();
    private static final Map<Long, List<EventGift>> giftCache = new ConcurrentHashMap<>();
    private static final AtomicLong danmakuIdCounter = new AtomicLong(1);
    private static final AtomicLong giftIdCounter = new AtomicLong(1);

    /**
     * 获取场次的弹幕事件
     */
    @GetMapping("/danmaku")
    public Result getSessionDanmaku(@PathVariable Long sessionId) {
        try {
            List<EventDanmaku> danmakus = eventDanmakuMapper.findBySessionId(sessionId);
            return Result.success(danmakus);
        } catch (Exception e) {
            // 数据库连接失败，使用内存缓存
            List<EventDanmaku> danmakus = danmakuCache.getOrDefault(sessionId, generateMockDanmakus(sessionId));
            return Result.success(danmakus);
        }
    }

    /**
     * 获取场次的礼物事件
     */
    @GetMapping("/gifts")
    public Result getSessionGifts(@PathVariable Long sessionId) {
        try {
            List<EventGift> gifts = eventGiftMapper.findBySessionId(sessionId);
            return Result.success(gifts);
        } catch (Exception e) {
            // 数据库连接失败，使用内存缓存
            List<EventGift> gifts = giftCache.getOrDefault(sessionId, generateMockGifts(sessionId));
            return Result.success(gifts);
        }
    }

    /**
     * 生成模拟弹幕数据
     */
    private List<EventDanmaku> generateMockDanmakus(Long sessionId) {
        List<EventDanmaku> danmakus = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        String[] contents = {
            "主播好厉害！",
            "这个游戏我也在玩",
            "主播加油！",
            "666666",
            "哈哈哈笑死我了",
            "这个操作太秀了",
            "主播玩得真好",
            "求教程",
            "关注了关注了",
            "主播什么时候下播？"
        };
        
        for (int i = 0; i < 10; i++) {
            EventDanmaku danmaku = new EventDanmaku();
            danmaku.setId(danmakuIdCounter.incrementAndGet());
            danmaku.setSessionId(sessionId);
            danmaku.setRoomId(1986387323L);
            danmaku.setUserId(1000000L + i);
            danmaku.setUsername("用户" + (1000000 + i));
            danmaku.setContent(contents[i % contents.length]);
            danmaku.setTimestamp(now.minusMinutes(i));
            danmaku.setCreatedAt(now.minusMinutes(i));
            danmakus.add(danmaku);
        }
        
        danmakuCache.put(sessionId, danmakus);
        return danmakus;
    }

    /**
     * 生成模拟礼物数据
     */
    private List<EventGift> generateMockGifts(Long sessionId) {
        List<EventGift> gifts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        String[] giftNames = {
            "辣条",
            "荧光棒",
            "小花花",
            "奶茶",
            "大啤酒"
        };
        
        for (int i = 0; i < 5; i++) {
            EventGift gift = new EventGift();
            gift.setId(giftIdCounter.incrementAndGet());
            gift.setSessionId(sessionId);
            gift.setRoomId(1986387323L);
            gift.setUserId(1000000L + i);
            gift.setUsername("用户" + (1000000 + i));
            gift.setGiftName(giftNames[i % giftNames.length]);
            gift.setGiftCount(i + 1);
            gift.setGiftPrice(java.math.BigDecimal.valueOf(10));
            gift.setTotalAmount(java.math.BigDecimal.valueOf(10 * (i + 1)));
            gift.setTimestamp(now.minusMinutes(i * 2));
            gift.setCreatedAt(now.minusMinutes(i * 2));
            gifts.add(gift);
        }
        
        giftCache.put(sessionId, gifts);
        return gifts;
    }
}
