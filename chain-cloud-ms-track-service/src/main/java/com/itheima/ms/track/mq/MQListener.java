package com.itheima.ms.track.mq;

import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.vo.TransportOrderMsg;
import com.itheima.chaincloud.common.vo.TransportOrderStatusMsg;
import com.itheima.ms.track.service.TrackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消息处理
 */
@Slf4j
@Component
public class MQListener {

    @Resource
    private TrackService trackService;

    /**
     * 创建运单后创建轨迹
     *
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.TRACK_TRANSPORT_ORDER_CREATED),
            exchange = @Exchange(name = Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED, type = ExchangeTypes.TOPIC, delayed = Constants.MQ.DELAYED),
            key = Constants.MQ.RoutingKeys.TRANSPORT_ORDER_CREATE
    ))
    public void listenTransportOrderCreatedMsg(String msg) {
        log.info("接收到新增运单的消息 ({})-> {}", Constants.MQ.Queues.TRACK_TRANSPORT_ORDER_CREATED, msg);
        TransportOrderMsg transportOrderMsg = JSONUtil.toBean(msg, TransportOrderMsg.class);
        this.trackService.create(transportOrderMsg.getId());
    }

    /**
     * 运单签收后完成轨迹
     *
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.TRACK_TRANSPORT_ORDER_UPDATE_STATUS),
            exchange = @Exchange(name = Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED, type = ExchangeTypes.TOPIC, delayed = Constants.MQ.DELAYED),
            key = Constants.MQ.RoutingKeys.TRANSPORT_ORDER_UPDATE_STATUS_PREFIX + "RECEIVED"
    ))
    public void listenTransportOrderUpdateStatusMsg(String msg) {
        log.info("接收到更新运单状态的消息 ({})-> {}", Constants.MQ.Queues.TRACK_TRANSPORT_ORDER_UPDATE_STATUS, msg);
        TransportOrderStatusMsg transportOrderStatusMsg = JSONUtil.toBean(msg, TransportOrderStatusMsg.class);
        this.trackService.complete(transportOrderStatusMsg.getIdList());
    }
}
