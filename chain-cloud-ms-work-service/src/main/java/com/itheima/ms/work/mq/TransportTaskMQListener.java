package com.itheima.ms.work.mq;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.api.base.truck.TruckPlanFeign;
import com.itheima.chaincloud.api.driver.DriverJobFeign;
import com.itheima.chaincloud.api.transport.TransportLineFeign;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.domain.transport.domain.TransportLineSearchDTO;
import com.itheima.chaincloud.domain.work.enums.TransportOrderSchedulingStatusEnum;
import com.itheima.chaincloud.domain.work.enums.TransportTaskAssignedStatusEnum;
import com.itheima.chaincloud.domain.work.enums.TransportTaskLoadingStatusEnum;
import com.itheima.chaincloud.domain.work.enums.TransportTaskStatusEnum;
import com.itheima.ms.work.entity.TransportOrderEntity;
import com.itheima.ms.work.entity.TransportOrderTaskEntity;
import com.itheima.ms.work.entity.TransportTaskEntity;
import com.itheima.ms.work.mq.bean.TransportTaskCreateMsgDTO;
import com.itheima.ms.work.service.TransportOrderService;
import com.itheima.ms.work.service.TransportOrderTaskService;
import com.itheima.ms.work.service.TransportTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransportTaskMQListener {

    private final DriverJobFeign driverJobFeign;
    private final TruckPlanFeign truckPlanFeign;
    private final TransportLineFeign transportLineFeign;
    private final TransportTaskService transportTaskService;
    private final TransportOrderTaskService transportOrderTaskService;
    private final TransportOrderService transportOrderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.WORK_TRANSPORT_TASK_CREATE),
            exchange = @Exchange(name = Constants.MQ.Exchanges.TRANSPORT_TASK, type = ExchangeTypes.TOPIC),
            key = Constants.MQ.RoutingKeys.TRANSPORT_TASK_CREATE
    ))
    public void listenTransportTaskMsg(String msg) {
        var transportTaskMsg = JSONUtil.toBean(msg, TransportTaskCreateMsgDTO.class);
        var driverIds = transportTaskMsg.getDriverIds();
        var assignedStatus = CollUtil.isEmpty(driverIds) ? TransportTaskAssignedStatusEnum.MANUAL_DISTRIBUTED : TransportTaskAssignedStatusEnum.DISTRIBUTED;
        var transportTaskId = this.createTransportTask(transportTaskMsg, assignedStatus);

        if (CollUtil.isEmpty(driverIds)) {
            log.info("生成司机作业单，司机列表为空，需要手动设置司机作业单 -> msg = {}", msg);
            return;
        }

        driverIds.forEach(driverId -> this.driverJobFeign.createDriverJob(transportTaskId, driverId));
    }

    @Transactional
    public Long createTransportTask(TransportTaskCreateMsgDTO transportTaskMsg, TransportTaskAssignedStatusEnum assignedStatus) {
        var truckPlanId = transportTaskMsg.getTruckPlanId();
        var truckPlanDto = truckPlanFeign.findById(truckPlanId);

        var transportTaskEntity = TransportTaskEntity.builder()
                .truckPlanId(transportTaskMsg.getTruckPlanId())
                .truckId(transportTaskMsg.getTruckId())
                .startAgencyId(transportTaskMsg.getStartOrganId())
                .endAgencyId(transportTaskMsg.getEndOrganId())
                .transportTripsId(transportTaskMsg.getTransportTripsId())
                .assignedStatus(assignedStatus)
                .planDepartureTime(truckPlanDto.getPlanDepartureTime())
                .planArrivalTime(truckPlanDto.getPlanArrivalTime())
                .status(TransportTaskStatusEnum.PENDING)
                .mark(JSONUtil.toJsonStr(Map.of("dispatchDecisionId", transportTaskMsg.getDispatchDecisionId())))
                .build();

        if (CollUtil.isEmpty(transportTaskMsg.getTransportOrderIdList())) {
            transportTaskEntity.setLoadingStatus(TransportTaskLoadingStatusEnum.EMPTY);
        } else {
            transportTaskEntity.setLoadingStatus(TransportTaskLoadingStatusEnum.FULL);
        }

        var transportLineSearchDTO = TransportLineSearchDTO.builder()
                .page(1).pageSize(1)
                .startOrganId(transportTaskEntity.getStartAgencyId())
                .endOrganId(transportTaskEntity.getEndAgencyId()).build();

        var transportLineResponse = this.transportLineFeign.queryPageList(transportLineSearchDTO);
        var transportLineDTO = CollUtil.getFirst(transportLineResponse.getItems());
        if (ObjectUtil.isNotEmpty(transportLineDTO)) {
            transportTaskEntity.setDistance(transportLineDTO.getDistance());
        }

        this.transportTaskService.save(transportTaskEntity);
        this.createTransportOrderTask(transportTaskEntity.getId(), transportTaskMsg);
        return transportTaskEntity.getId();
    }

    private void createTransportOrderTask(final Long transportTaskId, final TransportTaskCreateMsgDTO transportTaskMsg) {
        var transportOrderIdList = transportTaskMsg.getTransportOrderIdList();
        if (CollUtil.isEmpty(transportOrderIdList)) return;

        var resultList = CollStreamUtil.toList(transportOrderIdList, transportOrderId ->
                TransportOrderTaskEntity.builder().transportOrderId(transportOrderId).transportTaskId(transportTaskId).build());
        this.transportOrderTaskService.batchSaveTransportOrder(resultList);

        var list = CollStreamUtil.toList(transportOrderIdList, transportOrderId ->
                TransportOrderEntity.builder().id(transportOrderId)
                        .schedulingStatus(TransportOrderSchedulingStatusEnum.SCHEDULED).build());
        this.transportOrderService.updateBatchById(list);
    }
}
