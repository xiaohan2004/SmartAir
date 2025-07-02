package com.backend.aop;

import com.backend.dao.mapper.SystemLogMapper;
import com.backend.entity.SystemLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect //切面类
public class LogAspect {

    @Around("execution(* com.backend.service.*.*(..))")
    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        SystemLog systemLog = new SystemLog();
        SystemLogMapper.insert(systemLog);

        log.info("AOP记录操作日志: {}", systemLog);

        return result;
    }

}
