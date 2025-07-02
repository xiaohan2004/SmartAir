package com.backend.controller;

import com.backend.common.Result;
import com.backend.service.DashboardService;
import com.backend.vo.DashboardData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    public Result getDashboardOverview() {
        DashboardData dashboardData = dashboardService.getDashboardData();
        return Result.success(dashboardData);
    }
}
