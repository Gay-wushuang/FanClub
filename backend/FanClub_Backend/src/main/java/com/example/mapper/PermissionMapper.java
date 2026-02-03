package com.example.mapper;

import com.example.pojo.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface PermissionMapper {

    /**
     * 根据ID查询权限
     */
    Permission findById(@Param("id") Integer id);

    /**
     * 查询所有权限
     */
    List<Permission> findAll();

    /**
     * 根据ID列表查询权限
     */
    List<Permission> findByIds(@Param("ids") List<Integer> ids);

    /**
     * 新增权限
     */
    int insert(Permission permission);

    /**
     * 更新权限
     */
    int update(Permission permission);

    /**
     * 删除权限
     */
    int delete(@Param("id") Integer id);

}
