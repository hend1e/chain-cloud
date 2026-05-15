package com.itheima.ms.dispatch.mq;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.api.aigc.AIGCFeign;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.ms.dispatch.dto.DispatchMsgDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

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

        var dispatchMsgDTO = JSONUtil.toBean(msg, DispatchMsgDTO.class);
        if (ObjectUtil.isEmpty(dispatchMsgDTO)) {
            return; // 防御性校验，避免空指针
        }

        // ================== 2. 提取关键字段 ==================
        // 用于标识一条运输线路（起点→终点）
        var startId = dispatchMsgDTO.getCurrentAgencyId();
        var endId = dispatchMsgDTO.getNextAgencyId();
        var transportOrderId = dispatchMsgDTO.getTransportOrderId();

        // ================== 3. 幂等控制（非常关键） ==================
        /*
          为什么要幂等？
          MQ是"至少一次投递"，可能重复消费

          解决方案：
          使用Redis Set记录已处理订单

          Redis SADD特点：
          - 新增成功 → 返回1
          - 已存在 → 返回0
         */
        String setRedisKey = this.getSetRedisKey(startId, endId);
        var setOps = this.stringRedisTemplate.boundSetOps(setRedisKey);

        Long count = setOps.add(transportOrderId);
        if (ObjectUtil.equals(count, 0L)) {
            // 已存在，说明是重复消息，直接丢弃
            return;
        }

        try {
            // ================== 4. 构建优先级队列（ZSet） ==================
            /*
              Redis ZSet：
              - value：运单（当前使用msg JSON）
              - score：优先级（由AI计算）

              score越大 → 优先级越高

              key示例：
              DISPATCH_ZSET_起点_终点
             */
            var zSetRedisKey = this.getZSetRedisKey(startId, endId);
            var zSetOps = this.stringRedisTemplate.boundZSetOps(zSetRedisKey);

            // 当前线路订单数量（作为AI特征）
            // 👉 注意：这里每次都会访问Redis，有一定性能开销
            var score = this.getScore(dispatchMsgDTO, Convert.toLong(zSetOps.size(), 0L));

            // ================== 5. 写入ZSet ==================
            // 将运单按score排序存储
            zSetOps.add(msg, score);

        } catch (Exception e) {
            /*
              ================== 6. 幂等回滚（非常关键） ==================

              如果这里不回滚，会出现：
              set.add成功，但zset.add失败 → 运单"丢失"

              回滚后：
              MQ可以重新消费该消息
             */
            setOps.remove(transportOrderId);

            // 抛异常 → MQ可能重试（多次失败后会进入死信队列）
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建ZSet Key（按线路隔离）
     */
    public String getZSetRedisKey(Long startId, Long endId) {
        return StrUtil.format("DISPATCH_ZSET_{}_{}", startId, endId);
    }

    /**
     * 构建幂等Set Key
     */
    public String getSetRedisKey(Long startId, Long endId) {
        return StrUtil.format("DISPATCH_SET_{}_{}", startId, endId);
    }

    /**
     * 计算运单优先级（核心逻辑）
     * <p>
     * 设计思路：
     * 👉 AI评分 + 规则补偿（等待时间）
     * <p>
     * 为什么要混合？
     * - AI可能不稳定
     * - 小订单可能长期排不到（饿死问题）
     */
    private Double getScore(DispatchMsgDTO dto, Long count) {
        try {
            // ================== 1. 构造AI特征 ==================
            var param = Map.of(
                    "total_weight", dto.getTotalWeight(),      // 重量
                    "total_volume", dto.getTotalVolume(),      // 体积
                    "created", dto.getCreated(),               // 创建时间
                    "route_order_count", count                 // 当前线路订单数
            );

            // ================== 2. 调用AI服务 ==================
            var response = this.aigcFeign.transportOrderDispatch(JSONUtil.toJsonStr(param));

            // 获取AI评分
            double aiScore = response.getScore();

            // ================== 3. 等待时间评分 ==================
            double waitScore = this.getWaitScore(dto);

            /*
              ================== 4. 融合评分 ==================

              80% AI + 20% 等待时间

              👉 常见工业策略：Hybrid Scoring
             */
            return aiScore * 0.8 + waitScore * 0.2;

        } catch (Exception e) {
            // AI失败兜底（系统不能完全依赖AI）
            log.error("AI评分失败, transportOrderId={}", dto.getTransportOrderId(), e);
        }

        // 降级：只按等待时间排序
        return this.getWaitScore(dto);
    }

    /**
     * 等待时间评分（防止订单饿死）
     * <p>
     * 使用指数函数：
     * score = 1 - e^(-t)
     * <p>
     * 特点：
     * - 前期增长快（短时间也能提升优先级）
     * - 后期趋于1（不会无限增长）
     * - 平滑（避免硬截断）
     */
    private double getWaitScore(DispatchMsgDTO dto) {
        // 当前等待时间（毫秒）
        long waitTime = System.currentTimeMillis() - dto.getCreated();

        // 转换为平滑增长的0~1分数
        return 1 - Math.exp(-waitTime / 3600000.0);
    }

}
