package com.example.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User implements Serializable {

    private Long id; // 用户ID
    private String username; // 用户名
    private String password; // 密码
    private String nickname; // 昵称
    private Integer roleId; // 角色ID
    private Integer status; // 状态：0禁用，1启用
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间

}
