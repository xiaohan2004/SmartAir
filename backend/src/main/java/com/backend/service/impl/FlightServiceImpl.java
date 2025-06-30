package com.backend.service.impl;

import com.backend.entity.Flight;
import com.backend.dao.mapper.FlightMapper;
import com.backend.service.FlightService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 航班服务实现类
 */
@Service
public class FlightServiceImpl extends ServiceImpl<FlightMapper, Flight> implements FlightService {

    @Override
    public Flight getByFlightNo(String flightNo) {
        LambdaQueryWrapper<Flight> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Flight::getFlightNo, flightNo);
        return getOne(queryWrapper);
    }

    @Override
    public List<Flight> searchFlights(String departureCity, String arrivalCity, 
                                    LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Flight> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Flight::getDepartureCity, departureCity)
                   .eq(Flight::getArrivalCity, arrivalCity)
                   .ge(Flight::getScheduledDepartureTime, startTime)
                   .le(Flight::getScheduledDepartureTime, endTime)
                   .orderByAsc(Flight::getScheduledDepartureTime);
        return list(queryWrapper);
    }

    @Override
    public List<Flight> listByAirline(String airline) {
        LambdaQueryWrapper<Flight> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Flight::getAirline, airline)
                   .orderByAsc(Flight::getScheduledDepartureTime);
        return list(queryWrapper);
    }
}