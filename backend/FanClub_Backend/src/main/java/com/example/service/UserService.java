package com.example.service;

import com.example.mapper.PermissionMapper;
import com.example.mapper.RoleMapper;
import com.example.mapper.UserMapper;
import com.example.pojo.User;
import com.example.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含token和用户信息
     */
    public Map<String, Object> login(String username, String password) {
        // 查询用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已禁用");
        }

        // 检查密码
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }

        // 查询角色
        var role = roleMapper.findById(user.getRoleId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 查询权限
        List<Integer> permIds = roleMapper.findPermissionsByRoleId(user.getRoleId());
        var permissions = permissionMapper.findByIds(permIds);

        // 生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("roleId", user.getRoleId());
        claims.put("roleName", role.getRoleName());

        String token = JwtUtils.generateToken(claims);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        result.put("role", role);
        result.put("permissions", permissions);

        return result;
    }

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    public User findById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    /**
     * 新增用户
     * @param user 用户信息
     * @return 影响行数
     */
    public int add(User user) {
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 设置默认值
        user.setStatus(1);
        user.setCreateTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());

        return userMapper.insert(user);
    }

    /**
     * 更新用户
     * @param user 用户信息
     * @return 影响行数
     */
    public int update(User user) {
        user.setUpdateTime(java.time.LocalDateTime.now());
        return userMapper.update(user);
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 影响行数
     */
    public int delete(Long id) {
        return userMapper.delete(id);
    }

    /**
     * 检查用户是否有指定权限
     * @param roleId 角色ID
     * @param permKey 权限标识
     * @return 是否有权限
     */
    public boolean hasPermission(Integer roleId, String permKey) {
        List<Integer> permIds = roleMapper.findPermissionsByRoleId(roleId);
        var permissions = permissionMapper.findByIds(permIds);

        for (var perm : permissions) {
            if (perm.getPermKey().equals(permKey)) {
                return true;
            }
        }

        return false;
    }

}
