package com.itheima.ms.work.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.chaincloud.domain.work.domain.response.TransportOrderStatusCountDTO;
import com.itheima.ms.work.entity.TransportOrderEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TransportOrderMapper extends BaseMapper<TransportOrderEntity> {

    List<TransportOrderStatusCountDTO> findStatusCount();
}
