package com.example.bilibili.service;

import com.example.bilibili.pojo.BilibiliRoomInitResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

@Service
public class BilibiliApiService {

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    public BilibiliApiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        this.objectMapper = new ObjectMapper();
    }

    /**
     * 根据房间ID获取真实的房间信息
     * @param id 直播间ID
     * @return 响应JSON对象
     * @throws IOException 网络请求异常
     */
    public Object getRoomInit(long id) throws IOException {
        String url = "https://api.live.bilibili.com/room/v1/Room/room_init?id=" + id;
        return executeWithRetry(url);
    }

    /**
     * 处理live_time异常值
     * @param responseObj 响应对象
     * @return 处理后的响应对象
     */
    private Object processLiveTime(Object responseObj) {
        try {
            // 使用Jackson的TreeNode来处理JSON，更可靠
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.valueToTree(responseObj);

            // 导航到内层data
            if (rootNode.has("data")) {
                com.fasterxml.jackson.databind.JsonNode dataNode = rootNode.get("data");
                if (dataNode.has("data")) {
                    com.fasterxml.jackson.databind.JsonNode innerDataNode = dataNode.get("data");
                    if (innerDataNode.has("live_time")) {
                        com.fasterxml.jackson.databind.JsonNode liveTimeNode = innerDataNode.get("live_time");
                        if (liveTimeNode.isNumber()) {
                            long liveTime = liveTimeNode.asLong();
                            if (liveTime < 0) {
                                // 创建一个可修改的对象
                                java.util.Map<String, Object> resultMap = new java.util.HashMap<>();
                                resultMap.put("code", rootNode.get("code").asInt());
                                resultMap.put("msg", rootNode.get("msg").asText());

                                java.util.Map<String, Object> outerDataMap = new java.util.HashMap<>();
                                outerDataMap.put("code", dataNode.get("code").asInt());
                                outerDataMap.put("msg", dataNode.get("msg").asText());
                                outerDataMap.put("message", dataNode.get("message").asText());

                                java.util.Map<String, Object> innerDataMap = new java.util.HashMap<>();
                                // 复制所有字段
                                Iterator<String> fieldNames = innerDataNode.fieldNames();
                                while (fieldNames.hasNext()) {
                                    String fieldName = fieldNames.next();
                                    com.fasterxml.jackson.databind.JsonNode fieldNode = innerDataNode.get(fieldName);
                                    if (fieldNode.isNumber()) {
                                        innerDataMap.put(fieldName, fieldNode.asLong());
                                    } else if (fieldNode.isBoolean()) {
                                        innerDataMap.put(fieldName, fieldNode.asBoolean());
                                    } else if (fieldNode.isTextual()) {
                                        innerDataMap.put(fieldName, fieldNode.asText());
                                    }
                                }

                                // 处理live_time
                                innerDataMap.put("live_time", 0);
                                innerDataMap.put("live_time_valid", false);

                                outerDataMap.put("data", innerDataMap);
                                resultMap.put("data", outerDataMap);

                                return resultMap;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseObj;
    }

    /**
     * 获取直播间信息
     * @param roomId 真实房间ID
     * @return 直播间信息JSON对象
     * @throws IOException 网络请求异常
     */
    public Object getRoomInfo(long roomId) throws IOException {
        String url = "https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom?room_id=" + roomId;
        return executeWithRetry(url);
    }

    /**
     * 获取直播间统计数据
     * @param roomId 真实房间ID
     * @return 统计数据JSON对象
     * @throws IOException 网络请求异常
     */
    public Object getRoomStats(long roomId) throws IOException {
        String url = "https://api.live.bilibili.com/xlive/web-room/v1/index/getRoomPlayInfo?room_id=" + roomId + "&protocol=0&format=0&codec=0&qn=0&platform=h5&ptype=8";
        return executeWithRetry(url);
    }

    /**
     * 执行HTTP请求并处理重试
     * @param url 请求URL
     * @return 响应JSON对象
     * @throws IOException 网络请求异常
     */
    private Object executeWithRetry(String url) throws IOException {
        int retries = 0;
        IOException lastException = null;

        while (retries < MAX_RETRIES) {
            try {
                return executeRequest(url);
            } catch (IOException e) {
                lastException = e;
                retries++;
                if (retries < MAX_RETRIES) {
                    System.out.println("请求失败，正在重试 (" + retries + "/" + MAX_RETRIES + "): " + e.getMessage());
                    try {
                        Thread.sleep(RETRY_DELAY_MS * retries); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("重试被中断", ie);
                    }
                }
            }
        }

        throw lastException;
    }

    /**
     * 执行单个HTTP请求
     * @param url 请求URL
     * @return 响应JSON对象
     * @throws IOException 网络请求异常
     */
    private Object executeRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();

            // 解析为JsonNode
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(responseBody);

            // 检查并处理live_time
            processLiveTimeNode(rootNode);

            // 将修改后的JsonNode转换回Object
            return objectMapper.treeToValue(rootNode, Object.class);
        }
    }

    /**
     * 处理live_time节点
     * @param rootNode 根节点
     */
    private void processLiveTimeNode(com.fasterxml.jackson.databind.JsonNode rootNode) {
        // 检查并处理live_time
        if (rootNode.has("data")) {
            com.fasterxml.jackson.databind.JsonNode dataNode = rootNode.get("data");
            
            // 处理直接在data中的live_time
            if (dataNode.has("live_time")) {
                processSingleLiveTimeNode(dataNode);
            }
            
            // 处理在room_info中的live_time
            if (dataNode.has("room_info")) {
                com.fasterxml.jackson.databind.JsonNode roomInfoNode = dataNode.get("room_info");
                if (roomInfoNode.has("live_time")) {
                    processSingleLiveTimeNode(roomInfoNode);
                }
            }
        }
    }

    /**
     * 处理单个live_time节点
     * @param node 包含live_time的节点
     */
    private void processSingleLiveTimeNode(com.fasterxml.jackson.databind.JsonNode node) {
        if (node.has("live_time")) {
            com.fasterxml.jackson.databind.JsonNode liveTimeNode = node.get("live_time");
            if (liveTimeNode.isNumber()) {
                long liveTime = liveTimeNode.asLong();
                if (liveTime < 0) {
                    // 创建一个可修改的ObjectNode
                    if (node instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                        com.fasterxml.jackson.databind.node.ObjectNode mutableNode = (com.fasterxml.jackson.databind.node.ObjectNode) node;
                        mutableNode.put("live_time", 0);
                        mutableNode.put("live_time_valid", false);
                    }
                } else {
                    if (node instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                        com.fasterxml.jackson.databind.node.ObjectNode mutableNode = (com.fasterxml.jackson.databind.node.ObjectNode) node;
                        mutableNode.put("live_time_valid", true);
                    }
                }
            }
        }
    }
}
