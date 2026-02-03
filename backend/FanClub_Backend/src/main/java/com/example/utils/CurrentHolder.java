package com.example.utils;

public class CurrentHolder {

    // 员工ID（保持向后兼容）
    private static final ThreadLocal<Integer> CURRENT_LOCAL = new ThreadLocal<>();
    
    // 用户ID
    private static final ThreadLocal<Long> USER_ID_LOCAL = new ThreadLocal<>();
    
    // 用户名
    private static final ThreadLocal<String> USERNAME_LOCAL = new ThreadLocal<>();
    
    // 角色ID
    private static final ThreadLocal<Integer> ROLE_ID_LOCAL = new ThreadLocal<>();

    // 旧方法（保持向后兼容）
    public static void setCurrentId(Integer employeeId) {
        CURRENT_LOCAL.set(employeeId);
    }

    public static Integer getCurrentId() {
        return CURRENT_LOCAL.get();
    }

    // 新方法
    public static void setUserId(Long userId) {
        USER_ID_LOCAL.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_LOCAL.get();
    }

    public static void setUsername(String username) {
        USERNAME_LOCAL.set(username);
    }

    public static String getUsername() {
        return USERNAME_LOCAL.get();
    }

    public static void setRoleId(Integer roleId) {
        ROLE_ID_LOCAL.set(roleId);
    }

    public static Integer getRoleId() {
        return ROLE_ID_LOCAL.get();
    }

    // 清理所有线程变量
    public static void remove() {
        CURRENT_LOCAL.remove();
        USER_ID_LOCAL.remove();
        USERNAME_LOCAL.remove();
        ROLE_ID_LOCAL.remove();
    }
}
