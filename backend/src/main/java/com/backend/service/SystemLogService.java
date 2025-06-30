package com.backend.service;

import com.backend.entity.SystemLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统日志服务接口
 */
public interface SystemLogService extends IService<SystemLog> {
    
    /**
     * 添加日志
     * @param message 日志消息
     * @return 是否成功
     */
    boolean addLog(String message);
    
    /**
     * 查询指定时间范围内的日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<SystemLog> listLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 清理指定时间之前的日志
     * @param beforeTime 指定时间
     * @return 清理的记录数
     */
    int cleanLogsBefore(LocalDateTime beforeTime);
}