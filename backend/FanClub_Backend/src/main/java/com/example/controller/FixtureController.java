package com.example.controller;

import com.example.mapper.EventDanmakuMapper;
import com.example.mapper.EventGiftMapper;
import com.example.mapper.SessionMapper;
import com.example.pojo.EventDanmaku;
import com.example.pojo.EventGift;
import com.example.pojo.Result;
import com.example.pojo.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 测试数据生成控制器
 */
@RestController
@RequestMapping("/api/v1/fixtures")
public class FixtureController {

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private EventDanmakuMapper eventDanmakuMapper;

    @Autowired
    private EventGiftMapper eventGiftMapper;

    private final Random random = new Random();

    /**
     * 生成测试数据
     */
    @PostMapping("/generate")
    public Result generateFixtures(@RequestParam(defaultValue = "1") int sessionCount) {
        try {
            // 生成测试场次
            List<Session> sessions = generateSessions(sessionCount);
            
            // 为每个场次生成测试弹幕和礼物
            for (Session session : sessions) {
                generateDanmakus(session.getId(), session.getRoomId());
                generateGifts(session.getId(), session.getRoomId());
            }
            
            return Result.success(sessions);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("生成测试数据失败: " + e.getMessage());
        }
    }

    /**
     * 生成测试场次
     */
    private List<Session> generateSessions(int count) {
        List<Session> sessions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < count; i++) {
            Session session = new Session();
            session.setRoomId(1986387323L + i);
            session.setAnchorId(672160178L + i);
            session.setPlatformUid(672160178L + i);
            session.setRoomTitle("测试直播间 " + (i + 1));
            session.setStartTime(now.minusHours(i));
            session.setEndTime(now.minusHours(i).plusHours(2));
            session.setDuration(7200);
            session.setStatus(2); // 已结束
            session.setOnlinePeak(1000 + random.nextInt(9000));
            session.setDanmakuCount(1000 + random.nextInt(9000));
            session.setGiftCount(100 + random.nextInt(900));
            session.setGiftAmount(new BigDecimal(1000 + random.nextInt(9000)));
            session.setViewerCount(10000 + random.nextInt(90000));
            session.setCreatedAt(now.minusHours(i));
            session.setUpdatedAt(now.minusHours(i).plusHours(2));
            
            sessionMapper.insert(session);
            sessions.add(session);
        }
        
        return sessions;
    }

    /**
     * 生成测试弹幕
     */
    private void generateDanmakus(Long sessionId, Long roomId) {
        List<EventDanmaku> danmakus = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);
        
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
        
        for (int i = 0; i < 100; i++) {
            EventDanmaku danmaku = new EventDanmaku();
            danmaku.setSessionId(sessionId);
            danmaku.setRoomId(roomId);
            danmaku.setUserId(1000000L + random.nextInt(9000000));
            danmaku.setUsername("用户" + (1000000 + random.nextInt(9000000)));
            danmaku.setContent(contents[random.nextInt(contents.length)]);
            danmaku.setTimestamp(startTime.plusMinutes(random.nextInt(120)));
            danmaku.setCreatedAt(danmaku.getTimestamp());
            
            danmakus.add(danmaku);
        }
        
        if (!danmakus.isEmpty()) {
            eventDanmakuMapper.batchInsert(danmakus);
        }
    }

    /**
     * 生成测试礼物
     */
    private void generateGifts(Long sessionId, Long roomId) {
        List<EventGift> gifts = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);
        
        String[] giftNames = {
            "辣条",
            "荧光棒",
            "小花花",
            "奶茶",
            "大啤酒",
            "火箭",
            "超级火箭",
            "飞机",
            "宇宙飞船",
            "嘉年华"
        };
        
        BigDecimal[] giftPrices = {
            new BigDecimal(1),
            new BigDecimal(1),
            new BigDecimal(9),
            new BigDecimal(29),
            new BigDecimal(59),
            new BigDecimal(500),
            new BigDecimal(2000),
            new BigDecimal(1000),
            new BigDecimal(5000),
            new BigDecimal(10000)
        };
        
        for (int i = 0; i < 50; i++) {
            EventGift gift = new EventGift();
            gift.setSessionId(sessionId);
            gift.setRoomId(roomId);
            gift.setUserId(1000000L + random.nextInt(9000000));
            gift.setUsername("用户" + (1000000 + random.nextInt(9000000)));
            
            int giftIndex = random.nextInt(giftNames.length);
            gift.setGiftName(giftNames[giftIndex]);
            gift.setGiftPrice(giftPrices[giftIndex]);
            
            int count = random.nextInt(10) + 1;
            gift.setGiftCount(count);
            gift.setTotalAmount(giftPrices[giftIndex].multiply(new BigDecimal(count)));
            
            gift.setTimestamp(startTime.plusMinutes(random.nextInt(120)));
            gift.setCreatedAt(gift.getTimestamp());
            
            gifts.add(gift);
        }
        
        if (!gifts.isEmpty()) {
            eventGiftMapper.batchInsert(gifts);
        }
    }
}
