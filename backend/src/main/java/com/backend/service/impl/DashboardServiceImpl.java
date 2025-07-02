package com.backend.service.impl;

import com.backend.dao.mapper.DashboardMapper;
import com.backend.service.DashboardService;
import com.backend.vo.DashboardData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public DashboardData getDashboardData() {
        DashboardData data = dashboardMapper.getBaseStats();

        List<String> dates = dashboardMapper.getRecentDates();
        List<Integer> flightCounts = dashboardMapper.getRecentDatesFlightCount();
        List<Integer> orderCounts = dashboardMapper.getRecentDatesOrderCount();

        data.setDates(dates);
        data.setDailyFlights(flightCounts);
        data.setDailyOrders(orderCounts);

        return data;
    }
}
