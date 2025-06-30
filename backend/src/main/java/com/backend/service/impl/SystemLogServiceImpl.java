package com.backend.service.impl;

import com.backend.entity.SystemLog;
import com.backend.dao.mapper.SystemLogMapper;
import com.backend.service.SystemLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统日志服务实现类
 */
@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog> implements SystemLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addLog(String message) {
        SystemLog log = new SystemLog();
        log.setMessage(message);
        log.setCreatedAt(LocalDateTime.now());
        return save(log);
    }

    @Override
    public List<SystemLog> listLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SystemLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(SystemLog::getCreatedAt, startTime)
                   .le(SystemLog::getCreatedAt, endTime)
                   .orderByDesc(SystemLog::getCreatedAt);
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanLogsBefore(LocalDateTime beforeTime) {
        LambdaQueryWrapper<SystemLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(SystemLog::getCreatedAt, beforeTime);
        
        // 先查询符合条件的记录数
        int count = (int) count(queryWrapper);
        
        // 执行删除
        remove(queryWrapper);
        
        return count;
    }
}