package com.backend.service;

import com.backend.entity.Flight;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 航班服务接口
 */
public interface FlightService extends IService<Flight> {
    
    /**
     * 根据航班号查询航班
     * @param flightNo 航班号
     * @return 航班信息
     */
    Flight getByFlightNo(String flightNo);
    
    /**
     * 查询指定日期范围内的航班
     * @param departureCity 出发城市
     * @param arrivalCity 到达城市
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 航班列表
     */
    List<Flight> searchFlights(String departureCity, String arrivalCity, 
                             LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据航空公司查询航班列表
     * @param airline 航空公司
     * @return 航班列表
     */
    List<Flight> listByAirline(String airline);
}