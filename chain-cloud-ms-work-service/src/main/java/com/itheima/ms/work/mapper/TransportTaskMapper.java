package com.itheima.ms.work.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.chaincloud.domain.work.domain.response.TransportTaskMonthlyDistanceDTO;
import com.itheima.chaincloud.domain.work.domain.response.TransportTaskStatusCountDTO;
import com.itheima.ms.work.entity.TransportTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TransportTaskMapper extends BaseMapper<TransportTaskEntity> {

    List<TransportTaskStatusCountDTO> findStatusCount();

    List<TransportTaskMonthlyDistanceDTO> monthlyDistanceStatistics(@Param("transportTaskIds") List<String> transportTaskIds,
                                                                    @Param("startTime") LocalDateTime startTime,
                                                                    @Param("endTime") LocalDateTime endTime);
}
