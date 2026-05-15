package com.itheima.ms.work.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.chaincloud.domain.work.domain.CourierTaskCountDTO;
import com.itheima.ms.work.entity.PickupDispatchTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TaskPickupDispatchMapper extends BaseMapper<PickupDispatchTaskEntity> {

    List<CourierTaskCountDTO> findCountByCourierIds(@Param("courierIds") List<Long> courierIds,
                                                    @Param("type") Integer type,
                                                    @Param("startDateTime") LocalDateTime startDateTime,
                                                    @Param("endDateTime") LocalDateTime endDateTime);
}
