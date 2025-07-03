package com.backend.service.impl;

import com.backend.service.AiService;
import com.backend.service.FlightOrderService;
import com.backend.vo.FlightOrderDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AiServiceImpl implements AiService {
    @Autowired
    FlightOrderService flightOrderService;

    @Override
    public void cancelFlightOrder(Long orderId, String name) {
        FlightOrderDetail flightOrderDetail = flightOrderService.getOrderDetailById(orderId);
//        if (flightOrderDetail.getScheduledDepartureTime().isBefore(LocalDateTime.now().plusDays(2))) {
//            throw new RuntimeException("航班起飞时间在两天内，无法取消订单");
//        }
        if (flightOrderDetail.getRealName().equals(name)) {
            boolean result = flightOrderService.cancelOrder(orderId);
            if (!result) {
                throw new RuntimeException("取消订单失败");
            }
        } else {
            throw new RuntimeException("姓名不匹配，无法取消订单");
        }
    }
}
