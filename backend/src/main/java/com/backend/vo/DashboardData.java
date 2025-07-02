package com.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardData {
    private Integer todayFlights;
    private Integer todayOrders;
    private Integer activeUsers;
    private Integer serviceTickets;

    private List<String> dates;
    private List<Integer> dailyFlights;
    private List<Integer> dailyOrders;
}
