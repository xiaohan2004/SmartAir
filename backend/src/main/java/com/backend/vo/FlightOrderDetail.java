package com.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightOrderDetail {
    // 基础订单信息
    private Long orderId;
    private String seatNo;
    private Integer status;
    private LocalDateTime createdAt;

    // 用户信息
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;

    // 航班信息
    private Long flightId;
    private String flightNo;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime scheduledDepartureTime;
    private LocalDateTime scheduledArrivalTime;
    private String airline;
    private String aircraftType;
    private BigDecimal price;
}
