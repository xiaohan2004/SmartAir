package com.backend.dao.mapper;

import com.backend.entity.FlightOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 航班订单Mapper接口
 */
@Mapper
public interface FlightOrderMapper extends BaseMapper<FlightOrder> {
    
}