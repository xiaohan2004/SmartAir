package com.backend.service;

import com.backend.entity.FlightOrder;
import com.backend.vo.FlightOrderDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 航班订单服务接口
 */
public interface FlightOrderService extends IService<FlightOrder> {
    
    /**
     * 根据用户ID查询订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    List<FlightOrder> listByUserId(Long userId);
    
    /**
     * 根据航班ID查询订单列表
     * @param flightId 航班ID
     * @return 订单列表
     */
    List<FlightOrder> listByFlightId(Long flightId);
    
    /**
     * 创建航班订单
     * @param userId 用户ID
     * @param flightId 航班ID
     * @param seatNo 座位号
     * @return 创建的订单
     */
    FlightOrder createOrder(Long userId, Long flightId, String seatNo);
    
    /**
     * 取消订单
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId);
    
    /**
     * 查询用户所有订单（包含航班和用户信息）
     * @param userId 用户ID
     * @return 订单列表（包含关联信息）
     */
    List<FlightOrder> listOrdersWithDetail(Long userId);

    /**
     * 根据订单ID获取订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    FlightOrderDetail getOrderDetailById(Long id);

    List<FlightOrderDetail> getFlightOrderDetailsByName(String realName);
}