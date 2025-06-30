package com.backend.dao.mapper;

import com.backend.entity.Flight;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 航班Mapper接口
 */
@Mapper
public interface FlightMapper extends BaseMapper<Flight> {
    
}