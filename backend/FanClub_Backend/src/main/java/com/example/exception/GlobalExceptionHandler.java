package com.example.exception;

import com.example.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        log.error("通用异常: {}, 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return Result.error("系统内部错误");
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: {}, 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return Result.error(e.getMessage());
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Result handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常: {}, 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return Result.error("系统内部错误: 空指针异常");
    }

    /**
     * 处理数据绑定异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("参数验证异常: {}, 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        
        return Result.error("参数验证失败", errors.toString());
    }

    /**
     * 处理SQL异常
     */
    @ExceptionHandler(SQLException.class)
    public Result handleSQLException(SQLException e, HttpServletRequest request) {
        log.error("SQL异常: {}, 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return Result.error("数据库操作失败");
    }

    /**
     * 处理重复键异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        log.error("重复键异常: {}, 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        String message = e.getMessage();
        if (message.contains("Duplicate entry")) {
            int i = message.indexOf("Duplicate entry");
            String errMsg = message.substring(i);
            String[] arr = errMsg.split(" ");
            return Result.error(arr[2] + " 已存在");
        }
        return Result.error("数据已存在");
    }

    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.error("资源不存在: {}, 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        return Result.notFound("接口不存在");
    }

    /**
     * 错误返回（指定错误码）
     */
    private Result error(Integer code, String msg) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

}

