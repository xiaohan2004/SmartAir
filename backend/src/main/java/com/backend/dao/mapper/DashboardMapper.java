package com.backend.dao.mapper;

import com.backend.vo.DashboardData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DashboardMapper {

    DashboardData getBaseStats();

    List<String> getRecentDates();

    List<Integer> getRecentDatesFlightCount();

    List<Integer> getRecentDatesOrderCount();
}
