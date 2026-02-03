package com.example.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体类
 */
@Data
public class Role implements Serializable {

    private Integer id; // 角色ID
    private String roleName; // 角色名称
    private String roleDesc; // 角色描述
    private Integer status; // 状态：0禁用，1启用
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间

    // 角色常量
    public static final Integer ANCHOR_ROLE_ID = 1; // 主播角色
    public static final Integer BROKER_ROLE_ID = 2; // 经纪人角色
    public static final Integer ADMIN_ROLE_ID = 3; // 管理员角色

    // 角色名称常量
    public static final String ANCHOR_ROLE_NAME = "主播";
    public static final String BROKER_ROLE_NAME = "经纪人";
    public static final String ADMIN_ROLE_NAME = "管理员";

}
