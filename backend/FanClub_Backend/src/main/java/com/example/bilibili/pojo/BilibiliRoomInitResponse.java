package com.example.bilibili.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BilibiliRoomInitResponse {
    
    private int code;
    private String message;
    private String msg;
    private Data data;
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getMsg() {
        return msg;
    }
    
    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public Data getData() {
        return data;
    }
    
    public void setData(Data data) {
        this.data = data;
    }
    
    public static class Data {
        
        @JsonProperty("room_id")
        private long roomId;
        
        @JsonProperty("short_id")
        private long shortId;
        
        private long uid;
        private String title;
        private String cover;
        private String online;
        private String live_status;
        private String area_id;
        private String area_name;
        private String parent_area_id;
        private String parent_area_name;
        private String user_cover;
        private String nickname;
        
        public long getRoomId() {
            return roomId;
        }
        
        public void setRoomId(long roomId) {
            this.roomId = roomId;
        }
        
        public long getShortId() {
            return shortId;
        }
        
        public void setShortId(long shortId) {
            this.shortId = shortId;
        }
        
        public long getUid() {
            return uid;
        }
        
        public void setUid(long uid) {
            this.uid = uid;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getCover() {
            return cover;
        }
        
        public void setCover(String cover) {
            this.cover = cover;
        }
        
        public String getOnline() {
            return online;
        }
        
        public void setOnline(String online) {
            this.online = online;
        }
        
        public String getLive_status() {
            return live_status;
        }
        
        public void setLive_status(String live_status) {
            this.live_status = live_status;
        }
        
        public String getArea_id() {
            return area_id;
        }
        
        public void setArea_id(String area_id) {
            this.area_id = area_id;
        }
        
        public String getArea_name() {
            return area_name;
        }
        
        public void setArea_name(String area_name) {
            this.area_name = area_name;
        }
        
        public String getParent_area_id() {
            return parent_area_id;
        }
        
        public void setParent_area_id(String parent_area_id) {
            this.parent_area_id = parent_area_id;
        }
        
        public String getParent_area_name() {
            return parent_area_name;
        }
        
        public void setParent_area_name(String parent_area_name) {
            this.parent_area_name = parent_area_name;
        }
        
        public String getUser_cover() {
            return user_cover;
        }
        
        public void setUser_cover(String user_cover) {
            this.user_cover = user_cover;
        }
        
        public String getNickname() {
            return nickname;
        }
        
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
        
        @JsonProperty("live_time")
        private long liveTime;
        
        public long getLiveTime() {
            return liveTime;
        }
        
        public void setLiveTime(long liveTime) {
            this.liveTime = liveTime;
        }
    }
}
