package com.itheima.ms.work.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.api.base.common.MQFeign;
import com.itheima.chaincloud.api.oms.OrderFeign;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.BeanUtil;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.util.StrUtil;
import com.itheima.chaincloud.common.vo.CourierMsg;
import com.itheima.chaincloud.common.vo.CourierTaskMsg;
import com.itheima.chaincloud.common.vo.TransportInfoMsg;
import com.itheima.chaincloud.domain.oms.enums.OrderStatusEnum;
import com.itheima.chaincloud.domain.work.enums.*;
import com.itheima.ms.work.entity.PickupDispatchTaskEntity;
import com.itheima.ms.work.service.PickupDispatchTaskService;
import com.itheima.ms.work.service.TransportOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 快递员的消息处理，该处理器处理两个消息：
 * 1. 生成快递员取派件任务
 * 2. 快递员取件成功，订单转运单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourierMQListener {

    private final PickupDispatchTaskService pickupDispatchTaskService;
    private final TransportOrderService transportOrderService;
    private final OrderFeign orderFeign;
    private final MQFeign mqFeign;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.WORK_PICKUP_DISPATCH_TASK_CREATE),
            exchange = @Exchange(name = Constants.MQ.Exchanges.PICKUP_DISPATCH_TASK_DELAYED, type = ExchangeTypes.TOPIC, delayed = Constants.MQ.DELAYED),
            key = Constants.MQ.RoutingKeys.PICKUP_DISPATCH_TASK_CREATE
    ))
    public void listenCourierTaskMsg(String msg) {
        log.info("接收到快递员任务的消息 >>> msg = {}", msg);
        CourierTaskMsg courierTaskMsg = JSONUtil.toBean(msg, CourierTaskMsg.class);

        var taskType = PickupDispatchTaskTypeEnum.codeOf(courierTaskMsg.getTaskType());
        var pickupDispatchTaskEntityList = this.pickupDispatchTaskService.findByOrderId(courierTaskMsg.getOrderId(), taskType);
        var list = StreamUtil.of(pickupDispatchTaskEntityList)
                .filter(entity -> entity.getStatus() == PickupDispatchTaskStatusEnum.NEW)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(list)) return;

        var orderDTO = this.orderFeign.findById(courierTaskMsg.getOrderId());
        if (ObjectUtil.isEmpty(orderDTO)) return;

        if (ObjectUtil.equalsAny(orderDTO.getStatus(), OrderStatusEnum.CANCELLED.getCode(), OrderStatusEnum.CLOSE.getCode(),
                OrderStatusEnum.DEL.getCode())) return;

        var pickupDispatchTask = BeanUtil.toBean(courierTaskMsg, PickupDispatchTaskEntity.class);
        pickupDispatchTask.setTaskType(taskType);
        var estimatedStartTime = LocalDateTimeUtil.offset(pickupDispatchTask.getEstimatedEndTime(), -1, ChronoUnit.HOURS);
        pickupDispatchTask.setEstimatedStartTime(estimatedStartTime);
        pickupDispatchTask.setSignStatus(PickupDispatchTaskSignStatusEnum.NOT_SIGNED);

        if (ObjectUtil.isNotEmpty(pickupDispatchTask.getCourierId())) {
            pickupDispatchTask.setAssignedStatus(PickupDispatchTaskAssignedStatusEnum.DISTRIBUTED);
        } else {
            pickupDispatchTask.setAssignedStatus(PickupDispatchTaskAssignedStatusEnum.MANUAL_DISTRIBUTED);
        }

        PickupDispatchTaskEntity result = this.pickupDispatchTaskService.saveTaskPickupDispatch(pickupDispatchTask);
        if (result == null) {
            throw new CCException(StrUtil.format("快递员任务保存失败 >>> msg = {}", msg));
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.WORK_COURIER_PICKUP_SUCCESS),
            exchange = @Exchange(name = Constants.MQ.Exchanges.COURIER, type = ExchangeTypes.TOPIC),
            key = Constants.MQ.RoutingKeys.COURIER_PICKUP
    ))
    public void listenCourierPickupMsg(String msg) {
        log.info("接收到快递员取件成功的消息 >>> msg = {}", msg);
        CourierMsg courierMsg = JSONUtil.toBean(msg, CourierMsg.class);
        var transportOrder = this.transportOrderService.orderToTransportOrder(courierMsg.getOrderId());

        //发送运单跟踪消息
        String info = StrUtil.format("智链云途快递员已取件， 取件人【{}，电话 {}】", courierMsg.getCourierName(), courierMsg.getCourierMobile());
        //构建消息实体类
        String transportInfoMsg = TransportInfoMsg.builder()
                .transportOrderId(transportOrder.getId())//运单id
                .status("已取件")//消息状态
                .info(info)//消息详情
                .created(DateUtil.current())//创建时间
                .build().toJson();
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_INFO, Constants.MQ.RoutingKeys.TRANSPORT_INFO_APPEND, transportInfoMsg);
    }
}
