package com.backend.controller;

import com.backend.common.Result;
import com.backend.common.enumeration.ResultCode;
import com.backend.entity.Flight;
import com.backend.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 航班控制器
 * 处理航班查询、添加、修改、删除等功能
 */
@RestController
@RequestMapping("/api/flight")
public class FlightController {

    @Autowired
    private FlightService flightService;

    /**
     * 获取航班列表
     *
     * @return 航班列表
     */
    @GetMapping("/list")
    public Result listFlights() {
        List<Flight> flights = flightService.list();
        return Result.success(flights);
    }

    /**
     * 获取航班详情
     *
     * @param id 航班ID
     * @return 航班详情
     */
    @GetMapping("/{id}")
    public Result getFlightById(@PathVariable Long id) {
        Flight flight = flightService.getById(id);
        if (flight == null) {
            return Result.error(ResultCode.NOT_FOUND, "航班不存在");
        }
        return Result.success(flight);
    }

    /**
     * 根据航班号查询航班
     *
     * @param flightNo 航班号
     * @return 航班信息
     */
    @GetMapping("/no/{flightNo}")
    public Result getFlightByNo(@PathVariable String flightNo) {
        Flight flight = flightService.getByFlightNo(flightNo);
        if (flight == null) {
            return Result.error(ResultCode.NOT_FOUND, "航班不存在");
        }
        return Result.success(flight);
    }

    /**
     * 搜索航班
     *
     * @param departureCity 出发城市
     * @param arrivalCity 到达城市
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 航班列表
     */
    @GetMapping("/search")
    public Result searchFlights(
            @RequestParam String departureCity,
            @RequestParam String arrivalCity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<Flight> flights = flightService.searchFlights(departureCity, arrivalCity, startTime, endTime);
        return Result.success(flights);
    }

    /**
     * 根据航空公司查询航班
     *
     * @param airline 航空公司
     * @return 航班列表
     */
    @GetMapping("/airline/{airline}")
    public Result listByAirline(@PathVariable String airline) {
        List<Flight> flights = flightService.listByAirline(airline);
        return Result.success(flights);
    }

    /**
     * 添加航班
     *
     * @param flight 航班信息
     * @return 添加结果
     */
    @PostMapping
    public Result addFlight(@RequestBody Flight flight) {
        // 检查航班号是否已存在
        Flight existingFlight = flightService.getByFlightNo(flight.getFlightNo());
        if (existingFlight != null) {
            return Result.error(ResultCode.PARAM_ERROR, "航班号已存在");
        }
        
        boolean success = flightService.save(flight);
        if (success) {
            return Result.success("添加航班成功", flight);
        } else {
            return Result.error("添加航班失败");
        }
    }

    /**
     * 更新航班信息
     *
     * @param id 航班ID
     * @param flight 航班信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result updateFlight(@PathVariable Long id, @RequestBody Flight flight) {
        Flight existingFlight = flightService.getById(id);
        if (existingFlight == null) {
            return Result.error(ResultCode.NOT_FOUND, "航班不存在");
        }
        
        // 检查航班号是否已被其他航班使用
        if (flight.getFlightNo() != null && !flight.getFlightNo().equals(existingFlight.getFlightNo())) {
            Flight flightWithSameNo = flightService.getByFlightNo(flight.getFlightNo());
            if (flightWithSameNo != null) {
                return Result.error(ResultCode.PARAM_ERROR, "航班号已被其他航班使用");
            }
        }
        
        boolean success = flightService.updateById(flight);
        if (success) {
            return Result.success("更新航班成功", flight);
        } else {
            return Result.error("更新航班失败");
        }
    }

    /**
     * 删除航班
     *
     * @param id 航班ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteFlight(@PathVariable Long id) {
        Flight flight = flightService.getById(id);
        if (flight == null) {
            return Result.error(ResultCode.NOT_FOUND, "航班不存在");
        }
        
        boolean success = flightService.removeById(id);
        if (success) {
            return Result.success("删除航班成功", null);
        } else {
            return Result.error("删除航班失败");
        }
    }
} 