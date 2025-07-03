package com.backend.service.impl;

import com.backend.entity.Flight;
import com.backend.entity.FlightOrder;
import com.backend.entity.User;
import com.backend.dao.mapper.FlightOrderMapper;
import com.backend.service.FlightOrderService;
import com.backend.service.FlightService;
import com.backend.service.UserService;
import com.backend.vo.FlightOrderDetail;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 航班订单服务实现类
 */
@Service
public class FlightOrderServiceImpl extends ServiceImpl<FlightOrderMapper, FlightOrder> implements FlightOrderService {

    @Autowired
    private UserService userService;

    @Autowired
    private FlightService flightService;
    @Autowired
    private FlightOrderMapper flightOrderMapper;

    @Override
    public List<FlightOrder> listByUserId(Long userId) {
        LambdaQueryWrapper<FlightOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlightOrder::getUserId, userId)
                   .orderByDesc(FlightOrder::getCreatedAt);
        return list(queryWrapper);
    }

    @Override
    public List<FlightOrder> listByFlightId(Long flightId) {
        LambdaQueryWrapper<FlightOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlightOrder::getFlightId, flightId)
                   .orderByDesc(FlightOrder::getCreatedAt);
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlightOrder createOrder(Long userId, Long flightId, String seatNo) {
        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查航班是否存在
        Flight flight = flightService.getById(flightId);
        if (flight == null) {
            throw new RuntimeException("航班不存在");
        }

        // 创建订单
        FlightOrder order = new FlightOrder();
        order.setUserId(userId);
        order.setFlightId(flightId);
        order.setSeatNo(seatNo);
        order.setStatus(1); // 默认已完成状态

        // 保存订单
        save(order);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId) {
        FlightOrder order = getById(orderId);
        if (order == null) {
            return false;
        }

        // 设置订单状态为已取消
        order.setStatus(2);

        return updateById(order);
    }

    @Override
    public List<FlightOrder> listOrdersWithDetail(Long userId) {
        // 查询用户订单
        List<FlightOrder> orders = listByUserId(userId);

        // 填充关联信息
        return orders.stream().map(order -> {
            // 设置用户信息
            User user = userService.getById(order.getUserId());
            order.setUser(user);

            // 设置航班信息
            Flight flight = flightService.getById(order.getFlightId());
            order.setFlight(flight);

            return order;
        }).collect(Collectors.toList());
    }

    @Override
    public FlightOrderDetail getOrderDetailById(Long id) {
        return flightOrderMapper.getOrderDetailById(id);
    }

    @Override
    public List<FlightOrderDetail> getFlightOrderDetailsByName(String realName) {
        // 查询用户ID
        User user = userService.getUserByRealName(realName);
        if (user == null) {
            return List.of();
        }

        // 查询用户所有订单
        List<FlightOrder> orders = listByUserId(user.getId());

        // 过滤掉已取消的订单
        orders = orders.stream()
                .filter(order -> order.getStatus() != 2) // 只保留未取消的订单
                .toList();

        // 获取订单详情
        return orders.stream()
                .map(order -> flightOrderMapper.getOrderDetailById(order.getId()))
                .collect(Collectors.toList());
    }
}