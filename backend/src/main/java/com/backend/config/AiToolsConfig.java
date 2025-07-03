package com.backend.config;

import com.backend.entity.FlightOrder;
import com.backend.service.AiService;
import com.backend.service.FlightOrderService;
import com.backend.vo.FlightOrderDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

@Configuration
public class AiToolsConfig {
    @Autowired
    AiService aiService;
    @Autowired
    FlightOrderService flightOrderService;

    public record CancelFlightOrderRequest(Long orderId, String name) {
    }

    public record FlightOrderDetailRequest(Long orderId, String name) {
    }

    public record FinishedFlightOrderDetailsRequest(String realName) {
    }

    @Bean
    @Description("处理航班订单/机票退订")
    public Function<CancelFlightOrderRequest, String> cancelFlightOrder() {
        return cancelFlightOrderRequest -> {
            aiService.cancelFlightOrder(cancelFlightOrderRequest.orderId, cancelFlightOrderRequest.name);
            return "退订成功！";
        };
    }

    @Bean
    @Description("获取航班订单/机票预定详细信息")
    public Function<FlightOrderDetailRequest, FlightOrderDetail> getFlightOrderDetail() {
        return request -> {
            try {
                return flightOrderService.getOrderDetailById(request.orderId);
            } catch (Exception e) {
                return new FlightOrderDetail(request.orderId(), null, null, null, null, request.name(),
                        null, null, null, null, null, null, null, null, null, null, null);
            }
        };
    }

    @Bean
    @Description("获取用户所有已预定的航班")
    public Function<FinishedFlightOrderDetailsRequest, List<FlightOrderDetail>> getFinishedFlightOrderDetails() {
        return request -> {
            try {
                return flightOrderService.getFlightOrderDetailsByName(request.realName());
            } catch (Exception e) {
                return List.of();
            }
        };
    }
}
