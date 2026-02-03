package com.example.mapper;

import com.example.pojo.Role;
import com.example.pojo.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface RoleMapper {

    /**
     * 根据ID查询角色
     */
    Role findById(@Param("id") Integer id);

    /**
     * 查询所有角色
     */
    List<Role> findAll();

    /**
     * 新增角色
     */
    int insert(Role role);

    /**
     * 更新角色
     */
    int update(Role role);

    /**
     * 删除角色
     */
    int delete(@Param("id") Integer id);

    /**
     * 根据角色ID查询权限
     */
    List<Integer> findPermissionsByRoleId(@Param("roleId") Integer roleId);

    /**
     * 为角色添加权限
     */
    int insertRolePermission(RolePermission rolePermission);

    /**
     * 删除角色的权限
     */
    int deleteRolePermissions(@Param("roleId") Integer roleId);

}
