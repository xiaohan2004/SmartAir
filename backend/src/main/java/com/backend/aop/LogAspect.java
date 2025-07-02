package com.backend.aop;

import com.backend.dao.mapper.SystemLogMapper;
import com.backend.entity.SystemLog;
import com.backend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Component
@Aspect //切面类
public class LogAspect {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private SystemLogMapper systemLogMapper;

    // 默认系统用户ID，用于无法获取用户ID时使用
    private static final Long DEFAULT_USER_ID = 0L;

    /**
     * 定义服务层切点
     */
    @Pointcut("execution(* com.backend.service.*.*(..))")
    public void servicePointcut() {}

    /**
     * 定义控制器层切点
     */
    @Pointcut("execution(* com.backend.controller.*.*(..))")
    public void controllerPointcut() {}

    /**
     * 定义用户相关操作切点
     */
    @Pointcut("execution(* com.backend.service.UserService.*(..)) || execution(* com.backend.service.impl.UserServiceImpl.*(..))")
    public void userOperationPointcut() {}

    /**
     * 定义订单相关操作切点
     */
    @Pointcut("execution(* com.backend.service.*Order*.*(..)) || execution(* com.backend.service.impl.*Order*.*(..))")
    public void orderOperationPointcut() {}

    /**
     * 定义航班相关操作切点
     */
    @Pointcut("execution(* com.backend.service.*Flight*.*(..)) || execution(* com.backend.service.impl.*Flight*.*(..))")
    public void flightOperationPointcut() {}

    /**
     * 获取当前登录用户ID
     * @return 用户ID，未获取到则返回默认值
     */
    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String jwt = request.getHeader("token");
                if (jwt != null && !jwt.isEmpty()) {
                    Claims claims = JwtUtil.parseJWT(jwt);
                    if (claims != null && claims.get("userId") != null) {
                        return Long.parseLong(claims.get("userId").toString());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取当前用户ID失败: {}", e.getMessage());
        }
        return DEFAULT_USER_ID;  // 返回默认用户ID
    }

    /**
     * 记录服务层方法执行日志
     */
    @Around("servicePointcut()")
    public Object recordServiceLog(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        String message = String.format("执行服务方法: %s.%s, 参数: %s, 耗时: %dms",
                className, methodName, Arrays.toString(args), executionTime);

        log.info(message);

        return result;
    }

    /**
     * 记录控制器层方法执行日志
     */
    @Around("controllerPointcut()")
    public Object recordControllerLog(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String requestURI = request != null ? request.getRequestURI() : "Unknown URI";
        String requestMethod = request != null ? request.getMethod() : "Unknown Method";
        String remoteAddr = request != null ? request.getRemoteAddr() : "Unknown IP";

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        String message = String.format("API请求: %s %s, 客户端IP: %s",
                requestMethod, requestURI, remoteAddr);

        log.info("{}，控制器: {}.{}，耗时: {}ms", message, className, methodName, executionTime);

        // 使用mapper直接记录日志
        SystemLog systemLog = new SystemLog();
        systemLog.setOperatorId(getCurrentUserId());
        systemLog.setClassName(className);
        systemLog.setMethodName(methodName);
        systemLog.setExecutionTime(executionTime);
        systemLog.setMessage(message);
        systemLog.setCreatedAt(LocalDateTime.now());
        systemLogMapper.insert(systemLog);

        return result;
    }

    /**
     * 记录用户相关操作日志
     */
    @Around("userOperationPointcut()")
    public Object recordUserOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();

        String operationDesc = null;

        // 记录特定用户操作
        if (methodName.contains("register") || methodName.contains("create")) {
            operationDesc = "用户注册";
        } else if (methodName.contains("login")) {
            operationDesc = "用户登录";
        } else if (methodName.contains("update") || methodName.contains("modify")) {
            operationDesc = "修改用户信息";
        } else if (methodName.contains("delete") || methodName.contains("remove")) {
            operationDesc = "删除用户";
        } else if (methodName.contains("password")) {
            operationDesc = "修改密码";
        }

        if (operationDesc != null) {
            logOperation(operationDesc, joinPoint);
        }

        return joinPoint.proceed();
    }

    /**
     * 记录订单相关操作日志
     */
    @Around("orderOperationPointcut()")
    public Object recordOrderOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();

        String operationDesc = null;

        // 记录特定订单操作
        if (methodName.contains("create") || methodName.contains("add")) {
            operationDesc = "创建订单";
        } else if (methodName.contains("cancel")) {
            operationDesc = "取消订单";
        } else if (methodName.contains("pay") || methodName.contains("payment")) {
            operationDesc = "支付订单";
        } else if (methodName.contains("refund")) {
            operationDesc = "退款订单";
        } else if (methodName.contains("change") || methodName.contains("modify")) {
            operationDesc = "修改订单";
        }

        if (operationDesc != null) {
            logOperation(operationDesc, joinPoint);
        }

        return joinPoint.proceed();
    }

    /**
     * 记录航班相关操作日志
     */
    @Around("flightOperationPointcut()")
    public Object recordFlightOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();

        String operationDesc = null;

        // 记录特定航班操作
        if (methodName.contains("add") || methodName.contains("create")) {
            operationDesc = "添加航班";
        } else if (methodName.contains("update") || methodName.contains("modify")) {
            operationDesc = "更新航班信息";
        } else if (methodName.contains("cancel")) {
            operationDesc = "取消航班";
        } else if (methodName.contains("delete") || methodName.contains("remove")) {
            operationDesc = "删除航班";
        }

        if (operationDesc != null) {
            logOperation(operationDesc, joinPoint);
        }

        return joinPoint.proceed();
    }

    /**
     * 记录异常日志
     */
    @AfterThrowing(pointcut = "servicePointcut() || controllerPointcut()", throwing = "e")
    public void recordExceptionLog(JoinPoint joinPoint, Throwable e) {
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();

        String message = String.format("异常发生: 异常类型=%s, 异常信息=%s",
                e.getClass().getName(), e.getMessage());

        log.error("异常发生: {}.{}, {}", className, methodName, message, e);

        // 使用mapper直接记录日志
        SystemLog systemLog = new SystemLog();
        systemLog.setOperatorId(getCurrentUserId());
        systemLog.setClassName(className);
        systemLog.setMethodName(methodName);
        systemLog.setMessage(message);
        systemLog.setCreatedAt(LocalDateTime.now());
        systemLogMapper.insert(systemLog);
    }

    /**
     * 记录操作日志的通用方法
     */
    private Object logOperation(String operationDesc, ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        String message = String.format("%s，参数: %s", operationDesc, Arrays.toString(args));

        log.info("{}，方法: {}.{}，耗时: {}ms", operationDesc, className, methodName, executionTime);

        // 使用mapper直接记录日志
        SystemLog systemLog = new SystemLog();
        systemLog.setOperatorId(getCurrentUserId());
        systemLog.setClassName(className);
        systemLog.setMethodName(methodName);
        systemLog.setExecutionTime(executionTime);
        systemLog.setMessage(message);
        systemLog.setCreatedAt(LocalDateTime.now());
        systemLogMapper.insert(systemLog);

        return result;
    }
}
