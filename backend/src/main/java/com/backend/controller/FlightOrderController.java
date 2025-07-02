package com.backend.controller;

import com.backend.common.Result;
import com.backend.common.enumeration.ResultCode;
import com.backend.entity.FlightOrder;
import com.backend.service.FlightOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 航班订单控制器
 * 处理订单查询、创建、取消等功能
 */
@RestController
@RequestMapping("/api/order")
public class FlightOrderController {

    @Autowired
    private FlightOrderService flightOrderService;

    /**
     * 获取订单详情
     *
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public Result getOrderById(@PathVariable Long id) {
        FlightOrder order = flightOrderService.getById(id);
        if (order == null) {
            return Result.error(ResultCode.NOT_FOUND, "订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 获取用户订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    @GetMapping("/user/{userId}")
    public Result listOrdersByUserId(@PathVariable Long userId) {
        List<FlightOrder> orders = flightOrderService.listByUserId(userId);
        return Result.success(orders);
    }

    /**
     * 获取用户订单详情列表（包含航班和用户信息）
     *
     * @param userId 用户ID
     * @return 订单详情列表
     */
    @GetMapping("/user/{userId}/detail")
    public Result listOrdersWithDetail(@PathVariable Long userId) {
        List<FlightOrder> orders = flightOrderService.listOrdersWithDetail(userId);
        return Result.success(orders);
    }

    /**
     * 获取航班订单列表
     *
     * @param flightId 航班ID
     * @return 订单列表
     */
    @GetMapping("/flight/{flightId}")
    public Result listOrdersByFlightId(@PathVariable Long flightId) {
        List<FlightOrder> orders = flightOrderService.listByFlightId(flightId);
        return Result.success(orders);
    }

    /**
     * 创建订单
     *
     * @param orderMap 订单信息
     * @return 创建结果
     */
    @PostMapping
    public Result createOrder(@RequestBody Map<String, Object> orderMap) {
        Long userId = Long.valueOf(orderMap.get("userId").toString());
        Long flightId = Long.valueOf(orderMap.get("flightId").toString());
        String seatNo = (String) orderMap.get("seatNo");
        
        // 参数校验
        if (userId == null || flightId == null || seatNo == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID、航班ID或座位号不能为空");
        }
        
        FlightOrder order = flightOrderService.createOrder(userId, flightId, seatNo);
        if (order != null) {
            return Result.success("订单创建成功", order);
        } else {
            return Result.error("订单创建失败");
        }
    }

    /**
     * 取消订单
     *
     * @param id 订单ID
     * @return 取消结果
     */
    @PutMapping("/{id}/cancel")
    public Result cancelOrder(@PathVariable Long id) {
        FlightOrder order = flightOrderService.getById(id);
        if (order == null) {
            return Result.error(ResultCode.NOT_FOUND, "订单不存在");
        }
        
        boolean success = flightOrderService.cancelOrder(id);
        if (success) {
            return Result.success("订单取消成功", null);
        } else {
            return Result.error("订单取消失败");
        }
    }

    /**
     * 获取所有订单列表（管理员使用）
     *
     * @return 订单列表
     */
    @GetMapping("/admin/list")
    public Result listAllOrders() {
        List<FlightOrder> orders = flightOrderService.list();
        return Result.success(orders);
    }
} 