package com.example.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限实体类
 */
@Data
public class Permission implements Serializable {

    private Integer id; // 权限ID
    private String permName; // 权限名称
    private String permKey; // 权限标识
    private String permDesc; // 权限描述
    private Integer status; // 状态：0禁用，1启用
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间

}
