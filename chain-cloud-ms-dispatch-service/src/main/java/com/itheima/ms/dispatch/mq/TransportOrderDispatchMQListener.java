package com.itheima.ms.dispatch.mq;

import com.itheima.chaincloud.api.aigc.AIGCFeign;
import com.itheima.chaincloud.common.constant.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 对于待调度运单消息的处理
 * 核心职责：
 * 1. 消费MQ中的运单消息
 * 2. 做幂等控制（防止重复消费）
 * 3. 调用AI服务计算优先级
 * 4. 将运单写入Redis ZSet，实现优先级排序
 * 👉 本类是"智能调度系统"的入口
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransportOrderDispatchMQListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final AIGCFeign aigcFeign;

    /**
     * MQ监听方法
     * <p>
     * 整体流程：
     * MQ消息 → 幂等校验 → AI评分 → 写入ZSet排序
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.DISPATCH_MERGE_TRANSPORT_ORDER),
            exchange = @Exchange(
                    name = Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED,
                    type = ExchangeTypes.TOPIC,
                    delayed = Constants.MQ.DELAYED
            ),
            key = Constants.MQ.RoutingKeys.JOIN_DISPATCH
    ))
    public void listenDispatchMsg(String msg) {

        // ================== 1. 消息解析 ==================
        // MQ传入JSON字符串 → 转DTO对象
        // 示例：
        // {"transportOrderId":"SL1000000000560","currentAgencyId":100280,"nextAgencyId":90001,"totalWeight":3.5,"totalVolume":2.1,"created":1652337676330}
        log.info("接收到新运单的消息 >>> msg = {}", msg);

        // TODO 待实现
    }

}
