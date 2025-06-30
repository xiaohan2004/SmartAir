package com.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统日志实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_log")
public class SystemLog {
    
    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 日志消息
     */
    private String message;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}