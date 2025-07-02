package com.backend.controller;

import com.backend.common.Result;
import com.backend.common.enumeration.ResultCode;
import com.backend.entity.SystemLog;
import com.backend.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统日志控制器
 * 处理系统日志的查询和管理功能
 */
@RestController
@RequestMapping("/api/admin/log")
public class SystemLogController {

    @Autowired
    private SystemLogService systemLogService;

    /**
     * 获取日志列表
     *
     * @return 日志列表
     */
    @GetMapping("/list")
    public Result listLogs() {
        List<SystemLog> logs = systemLogService.list();
        return Result.success(logs);
    }

    /**
     * 根据时间范围查询日志
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    @GetMapping("/search")
    public Result searchLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<SystemLog> logs = systemLogService.listLogsByTimeRange(startTime, endTime);
        return Result.success(logs);
    }

    /**
     * 添加日志
     *
     * @param params 包含日志消息的Map
     * @return 添加结果
     */
    @PostMapping
    public Result addLog(@RequestBody Map<String, String> params) {
        String message = params.get("message");
        
        // 参数校验
        if (message == null) {
            return Result.error(ResultCode.PARAM_ERROR, "日志消息不能为空");
        }
        
        boolean success = systemLogService.addLog(message);
        if (success) {
            return Result.success("日志添加成功", null);
        } else {
            return Result.error("日志添加失败");
        }
    }

    /**
     * 清理指定时间之前的日志
     *
     * @param params 包含时间的Map
     * @return 清理结果
     */
    @DeleteMapping("/clean")
    public Result cleanLogsBefore(@RequestBody Map<String, String> params) {
        String beforeTimeStr = params.get("beforeTime");
        
        // 参数校验
        if (beforeTimeStr == null) {
            return Result.error(ResultCode.PARAM_ERROR, "时间参数不能为空");
        }
        
        LocalDateTime beforeTime = LocalDateTime.parse(beforeTimeStr);
        int count = systemLogService.cleanLogsBefore(beforeTime);
        
        return Result.success("成功清理 " + count + " 条日志", count);
    }

    /**
     * 获取日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    @GetMapping("/{id}")
    public Result getLogById(@PathVariable Long id) {
        SystemLog log = systemLogService.getById(id);
        if (log == null) {
            return Result.error(ResultCode.NOT_FOUND, "日志不存在");
        }
        return Result.success(log);
    }
} 