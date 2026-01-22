// java
package com.example.aop;

import com.example.pojo.OperateLog;
import com.example.mapper.OperateLogMapper;
import com.example.utils.CurrentHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class OperateLogAspect {

    private static final int MAX_LEN = 2000;

    @Autowired
    private OperateLogMapper operateLogMapper;

    private final ObjectMapper objectMapper;

    public OperateLogAspect() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

//    @Pointcut("execution(public * com.example.controller..*(..))")
//    public void controllerMethods() {
//    }

    @Around("@annotation(com.example.anno.Log)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object returnValue = null;
        Throwable toThrow = null;

        try {
            returnValue = pjp.proceed();
            return returnValue;
        } catch (Throwable ex) {
            toThrow = ex;
            throw ex;
        } finally {
            long cost = System.currentTimeMillis() - start;
            saveLog(pjp, returnValue, toThrow, cost);
        }
    }

    private void saveLog(ProceedingJoinPoint pjp, Object returnValue, Throwable throwable, long cost) {
        try {
            OperateLog olog = new OperateLog();
            // 操作时间
            olog.setOperateTime(LocalDateTime.now());
            // 操作人ID：尝试从请求头或 request attribute 获取
            Integer empId = resolveUserIdFromRequest();
            olog.setOperateEmpId(empId);

            // 类名与方法名
            String className = pjp.getTarget() != null ? pjp.getTarget().getClass().getName() : pjp.getSignature().getDeclaringTypeName();
            String methodName = pjp.getSignature().getName();
            olog.setClassName(truncate(className));
            olog.setMethodName(truncate(methodName));

            // 参数
            String paramsJson = safeToJson(pjp.getArgs());
            olog.setMethodParams(truncate(paramsJson));

            // 返回值或异常信息
            String returnJson;
            if (throwable != null) {
                returnJson = throwable.getClass().getName() + ": " + truncate(throwable.getMessage());
            } else {
                returnJson = safeToJson(returnValue);
            }
            olog.setReturnValue(truncate(returnJson));

            olog.setCostTime(cost);

            log.info("记录操作日志: {}", olog);

            // 调用 mapper 保存（假定 insert 方法接收 OperateLog）
            operateLogMapper.insert(olog);
        } catch (Exception e) {
            // 记录日志本身不能影响主流程，忽略或使用框架日志记录（此处不打印）
        }
    }

    private String safeToJson(Object obj) {
        try {
            if (obj == null) return null;
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            try {
                return obj.toString();
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        if (s.length() <= MAX_LEN) return s;
        return s.substring(0, MAX_LEN);
    }

    private Integer resolveUserIdFromRequest() {
        return CurrentHolder.getCurrentId();
    }
}

