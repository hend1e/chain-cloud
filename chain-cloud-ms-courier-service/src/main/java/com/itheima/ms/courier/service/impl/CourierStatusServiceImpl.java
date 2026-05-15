package com.itheima.ms.courier.service.impl;

import com.itheima.chaincloud.api.work.PickupDispatchTaskFeign;
import com.itheima.chaincloud.common.util.BeanUtil;
import com.itheima.chaincloud.domain.courier.domain.CourierStatusDTO;
import com.itheima.ms.courier.service.CourierStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 快递员状态服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourierStatusServiceImpl implements CourierStatusService {

    private final PickupDispatchTaskFeign pickupDispatchTaskFeign;

    @Override
    public CourierStatusDTO getCourierStatus(Long courierId) {
        log.info("查询快递员状态，courierId: {}", courierId);

        return this.generateCourierStatus(courierId);
    }

    /**
     * 组装快递员状态数据，没有的暂时为null
     *
     * @param courierId 快递员ID
     * @return 快递员状态数据
     */
    private CourierStatusDTO generateCourierStatus(Long courierId) {
        CourierStatusDTO status = new CourierStatusDTO();
        status.setCourierId(courierId);
//        status.setStatus("busy");
        status.setDataFreshnessMs(0L);

        // 设置位置信息
        CourierStatusDTO.LocationInfo location = new CourierStatusDTO.LocationInfo();
//        location.setLat(52.3702);
//        location.setLng(4.8952);
//        location.setAddress("Amsterdam Centraal附近");
//        location.setUpdatedAt(LocalDateTime.now());
        status.setLocation(location);

        // 设置任务统计信息
        var taskStats = this.pickupDispatchTaskFeign.getCourierTaskStats(courierId);
        CourierStatusDTO.TaskStats tasks = BeanUtil.toBean(taskStats, CourierStatusDTO.TaskStats.class);
        status.setTasks(tasks);

        // 设置可用性信息
        CourierStatusDTO.AvailabilityInfo availability = new CourierStatusDTO.AvailabilityInfo();
        availability.setIsIdle(taskStats.getIsIdle());
        availability.setIdleDurationMin(taskStats.getIdleDurationMin());
//        availability.setNextAvailableTime(LocalDateTime.now().plusHours(2).plusMinutes(30));
//        availability.setTodayWorkingDurationSec(18000L);
        status.setAvailability(availability);

        // 设置载重能力信息
        CourierStatusDTO.CapacityInfo capacity = new CourierStatusDTO.CapacityInfo();
//        capacity.setCurrentLoad(12);
//        capacity.setMaxLoad(30);
//        capacity.setLoadRatio(0.4);
//        capacity.setVehicleType("electricBike");
        status.setCapacity(capacity);

        // 设置配送路径信息
        CourierStatusDTO.RouteInfo route = new CourierStatusDTO.RouteInfo();
//        route.setRouteId("R8891");
//        route.setNextStop("ORDER_99821");
//        route.setEtaNextStopMin(8);
//        route.setRemainingDistanceMeters(1200);
        status.setRoute(route);

        // 设置绩效信息
        CourierStatusDTO.PerformanceInfo performance = new CourierStatusDTO.PerformanceInfo();
//        performance.setOnTimeRate(0.96);
//        performance.setAvgDeliveryTimeMin(18);
//        performance.setComplaintCount(1);
//        performance.setRating(4.8);
        status.setPerformance(performance);

        return status;
    }
}
