package com.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 航班信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("flight")
public class Flight {
    
    /**
     * 航班ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 航班号
     */
    private String flightNo;
    
    /**
     * 航空公司
     */
    private String airline;
    
    /**
     * 出发城市
     */
    private String departureCity;
    
    /**
     * 到达城市
     */
    private String arrivalCity;
    
    /**
     * 计划出发时间
     */
    private LocalDateTime scheduledDepartureTime;
    
    /**
     * 计划到达时间
     */
    private LocalDateTime scheduledArrivalTime;
    
    /**
     * 飞机型号
     */
    private String aircraftType;
    
    /**
     * 航班价格
     */
    private BigDecimal price;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}