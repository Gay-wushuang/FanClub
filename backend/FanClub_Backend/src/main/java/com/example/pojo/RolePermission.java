package com.example.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色权限关联实体类
 */
@Data
public class RolePermission implements Serializable {

    private Integer id; // 主键ID
    private Integer roleId; // 角色ID
    private Integer permId; // 权限ID

}
