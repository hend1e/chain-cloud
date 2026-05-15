package com.itheima.ms.work.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.work.domain.*;
import com.itheima.chaincloud.domain.work.domain.request.PickupDispatchTaskPageQueryDTO;
import com.itheima.chaincloud.domain.work.domain.response.PickupDispatchTaskStatisticsDTO;
import com.itheima.chaincloud.domain.work.enums.*;
import com.itheima.ms.work.entity.PickupDispatchTaskEntity;

import java.util.List;

public interface PickupDispatchTaskService extends IService<PickupDispatchTaskEntity> {

    Boolean updateStatus(PickupDispatchTaskDTO pickupDispatchTaskDTO);

    Boolean updateCourierId(List<Long> ids, Long originalCourierId, Long targetCourierId);

    PickupDispatchTaskEntity saveTaskPickupDispatch(PickupDispatchTaskEntity taskPickupDispatch);

    PageResponse<PickupDispatchTaskDTO> findByPage(PickupDispatchTaskPageQueryDTO dto);

    List<CourierTaskCountDTO> findCountByCourierIds(List<Long> courierIds, PickupDispatchTaskTypeEnum pickupDispatchTaskType, String date);

    List<PickupDispatchTaskEntity> findByOrderId(Long orderId, PickupDispatchTaskTypeEnum taskType);

    boolean deleteByIds(List<Long> ids);

    Integer todayTasksCount(Long courierId, PickupDispatchTaskTypeEnum taskType, PickupDispatchTaskStatusEnum status, PickupDispatchTaskIsDeletedEnum isDeleted);

    List<PickupDispatchTaskDTO> findAll(Long courierId, PickupDispatchTaskTypeEnum taskType, PickupDispatchTaskStatusEnum taskStatus, PickupDispatchTaskIsDeletedEnum isDeleted);

    PickupDispatchTaskStatisticsDTO todayTaskStatistics(Long courierId);

    TaskStatsDTO getCourierTaskStats(Long courierId);
}
