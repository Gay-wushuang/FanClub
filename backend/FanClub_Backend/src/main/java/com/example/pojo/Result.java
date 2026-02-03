package com.example.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 */
@Data
public class Result implements Serializable {

    // 状态码常量
    public static final Integer SUCCESS = 1;
    public static final Integer ERROR = 0;
    public static final Integer UNAUTHORIZED = 40001;
    public static final Integer FORBIDDEN = 40003;
    public static final Integer NOT_FOUND = 40004;
    public static final Integer SERVER_ERROR = 50000;

    private Integer code; // 编码：1成功，0失败，其他为错误码
    private String msg; // 错误信息
    private Object data; // 数据

    /**
     * 成功返回（无数据）
     */
    public static Result success() {
        Result result = new Result();
        result.code = SUCCESS;
        result.msg = "success";
        return result;
    }

    /**
     * 成功返回（带数据）
     */
    public static Result success(Object object) {
        Result result = new Result();
        result.data = object;
        result.code = SUCCESS;
        result.msg = "success";
        return result;
    }

    /**
     * 错误返回（默认错误码）
     */
    public static Result error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = ERROR;
        return result;
    }

    /**
     * 错误返回（指定错误码）
     */
    public static Result error(Integer code, String msg) {
        Result result = new Result();
        result.code = code;
        result.msg = msg;
        return result;
    }

    /**
     * 未授权错误
     */
    public static Result unauthorized(String msg) {
        return error(UNAUTHORIZED, msg != null ? msg : "未授权访问");
    }

    /**
     * 禁止访问错误
     */
    public static Result forbidden(String msg) {
        return error(FORBIDDEN, msg != null ? msg : "禁止访问");
    }

    /**
     * 资源不存在错误
     */
    public static Result notFound(String msg) {
        return error(NOT_FOUND, msg != null ? msg : "资源不存在");
    }

    /**
     * 服务器内部错误
     */
    public static Result serverError(String msg) {
        return error(SERVER_ERROR, msg != null ? msg : "服务器内部错误");
    }

    /**
     * 错误返回（带数据）
     */
    public static Result error(String msg, Object data) {
        Result result = new Result();
        result.code = ERROR;
        result.msg = msg;
        result.data = data;
        return result;
    }

    /**
     * 错误返回（指定错误码和数据）
     */
    public static Result error(Integer code, String msg, Object data) {
        Result result = new Result();
        result.code = code;
        result.msg = msg;
        result.data = data;
        return result;
    }

}
