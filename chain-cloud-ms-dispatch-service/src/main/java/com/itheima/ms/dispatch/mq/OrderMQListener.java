package com.itheima.ms.dispatch.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.api.base.common.MQFeign;
import com.itheima.chaincloud.api.courier.CourierFeign;
import com.itheima.chaincloud.api.transport.DispatchConfigurationFeign;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.util.BeanUtil;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.vo.CourierTaskMsg;
import com.itheima.chaincloud.common.vo.OrderMsg;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 订单业务消息，接收到新订单后，根据快递员的负载情况，分配快递员
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMQListener {

    private final CourierFeign courierFeign;
    private final DispatchConfigurationFeign dispatchConfigurationFeign;
    private final MQFeign mqFeign;

    /**
     * 如果有多个快递员，需要通过AI智能选择快递员
     * <p>
     * 发送生成取件任务时需要计算时间差，如果小于2小时，实时发送；大于2小时，延时发送
     * 举例：
     * 1、现在10:30分，用户期望：11:00 ~ 12:00上门，实时发送
     * 2、现在10:30分，用户期望：13:00 ~ 14:00上门，延时发送，12点发送消息，延时1.5小时发送
     *
     * @param msg 消息内容
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.DISPATCH_ORDER_TO_PICKUP_DISPATCH_TASK),
            exchange = @Exchange(name = Constants.MQ.Exchanges.ORDER_DELAYED, type = ExchangeTypes.TOPIC, delayed = Constants.MQ.DELAYED),
            key = Constants.MQ.RoutingKeys.ORDER_CREATE
    ))
    public void listenOrderMsg(String msg) {
        //{"orderId":123, "agencyId": 8001, "taskType":1, "mark":"带包装", "longitude":116.111, "latitude":39.00, "created":1654224658728, "estimatedEndTime": 1654224658728}
        log.info("接收到订单的消息 >>> msg = {}", msg);
        //1. 解析消息
        var orderMsg = JSONUtil.toBean(msg, OrderMsg.class);
        var agencyId = orderMsg.getAgencyId();
        var longitude = orderMsg.getLongitude();
        var latitude = orderMsg.getLatitude();
        var epochMilli = LocalDateTimeUtil.toEpochMilli(orderMsg.getEstimatedEndTime());

        //2. 查询有排班、符合条件的快递员，并且选择快递员
        var courierIds = this.courierFeign.queryCourierIdListByCondition(agencyId, longitude, latitude, epochMilli);
        Long selectedCourierId = null;
        if (CollUtil.isNotEmpty(courierIds)) {
            //选择快递员
            selectedCourierId = this.selectCourierByAI(courierIds, orderMsg.getTaskType(), longitude, latitude);
        }

        //3. 如果是取件任务，需要计算时间差，来决定是发送实时消息还是延时消息
        // 假设现在的时间是：10:30，用户期望上门时间是13:00 ~ 14:00
        var diffMinutes = LocalDateTimeUtil.between(LocalDateTimeUtil.now(), orderMsg.getEstimatedEndTime(), ChronoUnit.MINUTES);
        var dispatchConfiguration = this.dispatchConfigurationFeign.findConfiguration();
        var dispatchTime = dispatchConfiguration.getDispatchTime() * 60;
        var delay = Constants.MQ.DEFAULT_DELAY;
        if (ObjectUtil.equals(orderMsg.getTaskType(), PickupDispatchTaskTypeEnum.PICKUP.getCode()) && diffMinutes > dispatchTime) {
            //延迟消息 13:00 向前推 2小时，得到11:00
            var date = LocalDateTimeUtil.offset(orderMsg.getEstimatedEndTime(), dispatchTime * -1L, ChronoUnit.MINUTES);
            //延迟的时间，单位：毫秒 计算： 0.5小时 * 60分钟 * 60秒 *  1000
            delay = Convert.toInt(LocalDateTimeUtil.between(LocalDateTimeUtil.now(), date, ChronoUnit.MILLIS));
        }

        //4. 发送消息，通知work微服务，用于创建快递员取派件任务
        //4.1 构建消息
        var courierTaskMsg = BeanUtil.toBeanIgnoreError(orderMsg, CourierTaskMsg.class);
        courierTaskMsg.setCourierId(selectedCourierId);
        courierTaskMsg.setCreated(System.currentTimeMillis());

        //4.2 发送消息
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.PICKUP_DISPATCH_TASK_DELAYED,
                Constants.MQ.RoutingKeys.PICKUP_DISPATCH_TASK_CREATE, courierTaskMsg.toJson(), delay);

    }

    private Long selectCourierByAI(List<Long> courierIds, Integer taskType, Double longitude, Double latitude) {
        if (courierIds.size() == 1) {
            return courierIds.get(0);
        }
        // TODO AI智能选择快递员
        return courierIds.get(1);
    }

}
