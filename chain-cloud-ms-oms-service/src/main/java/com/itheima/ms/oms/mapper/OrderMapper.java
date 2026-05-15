package com.itheima.ms.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.ms.oms.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {

}
