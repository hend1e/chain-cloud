package com.itheima.ms.web.courier.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.itheima.chaincloud.api.track.TrackFeign;
import com.itheima.chaincloud.api.work.PickupDispatchTaskFeign;
import com.itheima.chaincloud.api.work.TransportOrderFeign;
import com.itheima.chaincloud.common.util.UserThreadLocal;
import com.itheima.chaincloud.domain.work.domain.PickupDispatchTaskDTO;
import com.itheima.chaincloud.domain.work.domain.TransportOrderDTO;
import com.itheima.chaincloud.domain.work.domain.request.PickupDispatchTaskPageQueryDTO;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskStatusEnum;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskTypeEnum;
import com.itheima.ms.web.courier.service.TrackService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.stream.Collectors;

@Service
public class TrackServiceImpl implements TrackService {
    @Resource
    private TrackFeign trackFeign;
    @Resource
    private PickupDispatchTaskFeign pickupDispatchTaskFeign;
    @Resource
    private TransportOrderFeign transportOrderFeign;

    /**
     * 快递员上报位置
     *
     * @param lng 经度
     * @param lat 纬度
     */
    @Override
    public Boolean uploadLocation(String lng, String lat) {
        //1. 获取当前用户id
        Long userId = UserThreadLocal.getUserId();

        //2. 根据快递员id查询关联的新建状态派件任务
        var pageQueryDTO = PickupDispatchTaskPageQueryDTO.builder()
                .page(1)
                .pageSize(999)
                .courierId(userId)
                //新建取件任务时，还未生成运单，所以只查询新建的派件任务。也只能上报派件任务的快递员位置
                .taskType(PickupDispatchTaskTypeEnum.DISPATCH)
                .status(PickupDispatchTaskStatusEnum.NEW)
                .build();
        var pageResponse = pickupDispatchTaskFeign.findByPage(pageQueryDTO);
        if (CollUtil.isEmpty(pageResponse.getItems())) {
            return true;
        }

        //3. 从取派件任务中提取订单号
        var orderIds = pageResponse.getItems().parallelStream().map(PickupDispatchTaskDTO::getOrderId).toArray(Long[]::new);

        //4. 根据订单id查询运单
        var transportOrderDTOS = transportOrderFeign.findByOrderIds(orderIds);

        //5. 从运单中提取运单号
        var transportOrderIds = transportOrderDTOS.parallelStream().map(TransportOrderDTO::getId).collect(Collectors.toList());

        //6. 上报位置
        return trackFeign.uploadFromCourier(transportOrderIds, Double.parseDouble(lng), Double.parseDouble(lat));
    }
}
