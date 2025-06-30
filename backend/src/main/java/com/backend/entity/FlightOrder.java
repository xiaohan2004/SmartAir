package com.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 航班订单实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("flight_order")
public class FlightOrder {
    
    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 航班ID
     */
    private Long flightId;
    
    /**
     * 座位号
     */
    private String seatNo;
    
    /**
     * 订单状态：1-购票成功(completed)，2-已取消(cancelled)
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 关联用户信息，非数据库字段
     */
    @TableField(exist = false)
    private User user;
    
    /**
     * 关联航班信息，非数据库字段
     */
    @TableField(exist = false)
    private Flight flight;
}