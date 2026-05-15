package com.itheima.ms.dispatch.job;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.api.aigc.AIGCFeign;
import com.itheima.chaincloud.api.base.common.MQFeign;
import com.itheima.chaincloud.api.base.truck.TruckPlanFeign;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.util.StrUtil;
import com.itheima.chaincloud.domain.base.truck.TruckDTO;
import com.itheima.chaincloud.domain.base.truck.TruckPlanDTO;
import com.itheima.ms.dispatch.dto.DispatchMsgDTO;
import com.itheima.ms.dispatch.ml.model.DispatchDecisionRecord;
import com.itheima.ms.dispatch.ml.model.DispatchResult;
import com.itheima.ms.dispatch.ml.model.FeatureSnapshot;
import com.itheima.ms.dispatch.ml.model.TruckInfo;
import com.itheima.ms.dispatch.ml.model.enums.DispatchStatus;
import com.itheima.ms.dispatch.mq.TransportOrderDispatchMQListener;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.NumberUtil.*;

/**
 * 调度运输任务（优化版）
 * <p>
 * 核心能力：
 * 1. 使用 Redis ZSet 实现"优先级调度"
 * 2. 支持跳过大单（避免阻塞）
 * 3. 批量拉取减少 Redis IO
 * 4. 无递归，避免栈溢出
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchJob {

    private final TransportOrderDispatchMQListener transportOrderDispatchMQListener;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final TruckPlanFeign truckPlanFeign;
    private final MQFeign mqFeign;
    private final AIGCFeign aigcFeign;

    /**
     * 容积使用比例（预留安全空间，避免装不下）
     */
    @Value("${cc.volume.ratio:0.95}")
    private Double volumeRatio;

    /**
     * 载重使用比例（避免超载）
     */
    @Value("${cc.weight.ratio:0.95}")
    private Double weightRatio;

    /**
     * XXL-Job入口
     * <p>
     * 分片广播模式：
     * 每个执行器只处理一部分车辆，避免重复调度
     */
    @XxlJob("transportTask")
    public void transportTask() {
        // 当前分片索引
        var shardIndex = XxlJobHelper.getShardIndex();

        // 总分片数
        var shardTotal = XxlJobHelper.getShardTotal();

        // 拉取"未分配任务"的车辆计划（已按分片过滤）
        var truckPlanList = this.truckPlanFeign.pullUnassignedPlan(shardTotal, shardIndex);

        if (CollUtil.isEmpty(truckPlanList)) {
            log.debug("无车辆可调度， shardIndex = {}，shardTotal = {}", shardIndex, shardTotal);
            return;
        }

        // 遍历每辆车
        for (var truckPlanDto : truckPlanList) {
            // 基础数据校验（防止脏数据）
            if (ObjectUtil.hasEmpty(
                    truckPlanDto.getStartOrganId(),
                    truckPlanDto.getEndOrganId(),
                    truckPlanDto.getTransportTripsId(),
                    truckPlanDto.getId())) {

                log.error("车辆计划数据异常: {}", truckPlanDto);
                continue;
            }

            var startOrganId = truckPlanDto.getStartOrganId();
            var endOrganId = truckPlanDto.getEndOrganId();

            // ZSet Key（同起点+终点共享一个队列）
            var redisKey = this.transportOrderDispatchMQListener.getZSetRedisKey(startOrganId, endOrganId);

            // 分布式锁Key（保证同一线路只被一个线程处理）
            var lockKey = Constants.LOCKS.DISPATCH_LOCK_PREFIX + redisKey;

            var lock = redissonClient.getFairLock(lockKey);

            // 本次调度结果（要分配给这辆车的运单）
            DispatchContext dispatchContext = new DispatchContext();
            try {
                // 阻塞获取锁（保证数据一致性）
                lock.lock();

                // 核心调度逻辑
                dispatchContext = this.executeTransportTask(redisKey, truckPlanDto.getTruckDTO());
            } catch (Exception e) {
                log.error("调度异常", e);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

            String dispatchDecisionId = StrUtil.EMPTY;
            try {
                // 发送训练数据
                var dispatchDecisionRecord = this.buildDecisionRecord(truckPlanDto.getTruckDTO(), dispatchContext);
                this.sendTrainData(dispatchDecisionRecord);
                dispatchDecisionId = dispatchDecisionRecord.getDispatchDecisionId();

                // 基于AI进行对此次调度结果的预测，目前仅打印日志，不做调度的干预
                var features = dispatchDecisionRecord.getFeatures();
                var response = this.aigcFeign.transportTaskDispatch(JSONUtil.toJsonStr(features));
                log.info("调度预测 - 车辆: {}, 路线: {}-{}, AI预测: {}", truckPlanDto.getTruckId(), startOrganId, endOrganId, response);
            } catch (Exception e) {
                log.warn("发送训练数据异常.", e);
            }

            // 这里不做空判断，也就是没有运单，也要生成运输任务
            // 生成运输任务
            this.createTransportTask(truckPlanDto, startOrganId, endOrganId, dispatchContext.getSelectedOrders(), dispatchDecisionId);
        }

        // 标记车辆计划已完成调度
        this.completeTruckPlan(truckPlanList);
    }

    /**
     * 核心调度算法（重点）
     * <p>
     * 设计目标：
     * 1. 按优先级（score）从高到低消费
     * 2. 避免"大单卡死队列"
     * 3. 减少 Redis 访问次数
     * 4. 防止死循环
     */
    private DispatchContext executeTransportTask(String redisKey, TruckDTO truckDto) {
        var resultList = new ArrayList<DispatchMsgDTO>();
        var zSetOps = stringRedisTemplate.boundZSetOps(redisKey);

        // 每次从Redis拉取的最大数量（减少IO）
        final int BATCH_SIZE = 20;

        // 当前已装载重量/体积
        var currentWeight = BigDecimal.ZERO;
        var currentVolume = BigDecimal.ZERO;

        // 车辆最大允许载重（带安全系数） 单位：kg
        var maxAllowableLoad = mul(truckDto.getAllowableLoad(), weightRatio, 1000);

        // 最大允许体积
        var maxAllowableVolume = mul(truckDto.getAllowableVolume(), volumeRatio);

        int skippedCount = 0;
        int rounds = 0;
        // 多轮扫描（避免一次拉取不够）
        while (true) {
            rounds++;
            // 1. 从ZSet取出"优先级最高"的一批数据（score从大到小）
            var tupleSet = zSetOps.reverseRangeWithScores(0, BATCH_SIZE - 1);
            if (CollUtil.isEmpty(tupleSet)) {
                // 队列已空
                break;
            }

            // 先全部 remove，避免并发重复消费
            var values = CollStreamUtil.toList(tupleSet, ZSetOperations.TypedTuple::getValue);
            zSetOps.remove(values.toArray());

            // 需要回退的数据（装不下的）
            var rollbackList = new ArrayList<ZSetOperations.TypedTuple<String>>();

            boolean hasAdded = false;

            // 2. 本地计算（不再频繁访问Redis）
            for (var tuple : tupleSet) {
                var data = tuple.getValue();
                var score = tuple.getScore();

                var dto = JSONUtil.toBean(data, DispatchMsgDTO.class);

                // 计算加入后的重量/体积
                var newWeight = add(currentWeight, BigDecimal.valueOf(dto.getTotalWeight()));
                var newVolume = add(currentVolume, BigDecimal.valueOf(dto.getTotalVolume()));

                // 3. 判断是否超出车辆运力
                if (isGreaterOrEqual(newWeight, maxAllowableLoad)
                        || isGreaterOrEqual(newVolume, maxAllowableVolume)) {
                    // 装不下，不丢弃，而是回退到ZSet （避免数据丢失 + 保持优先级）
                    rollbackList.add(new DefaultTypedTuple<>(data, score));
                    skippedCount++;   // 跳过计数器+1
                    continue;
                }

                // ✅ 可以装
                resultList.add(dto);
                currentWeight = newWeight;
                currentVolume = newVolume;
                hasAdded = true;
            }

            // 4. 批量回填未使用数据
            if (CollUtil.isNotEmpty(rollbackList)) {
                zSetOps.add(new HashSet<>(rollbackList));
            }

            /*
              5. 终止条件

              如果这一轮一个都没装进去：
              → 说明剩下的都装不下
              → 再循环也没意义（避免死循环）
             */
            if (!hasAdded) {
                break;
            }

            // 接近满载 → 提前结束（避免无意义扫描）
            if (isGreaterOrEqual(currentWeight, mul(maxAllowableLoad, 0.98))
                    || isGreaterOrEqual(currentVolume, mul(maxAllowableVolume, 0.98))) {
                break;
            }
        }

        return DispatchContext.builder()
                .selectedOrders(resultList)
                .skippedCount(skippedCount)
                .rounds(rounds)
                .remainingCount(zSetOps.size()) // 剩余数量
                .remainingHighPriorityRatio(0.0) // 暂时不填写，后续完善
                .build();
    }

    /**
     * 创建运输任务（发送MQ）
     */
    private void createTransportTask(TruckPlanDTO truckPlanDto,
                                     Long startOrganId,
                                     Long endOrganId,
                                     List<DispatchMsgDTO> dispatchList,
                                     String dispatchDecisionId) {

        // 运单ID列表
        var orderIds = CollStreamUtil.toList(dispatchList, DispatchMsgDTO::getTransportOrderId);

        // 司机列表（保证不为null）
        var driverIds = CollUtil.isNotEmpty(truckPlanDto.getDriverIds()) ? truckPlanDto.getDriverIds() : ListUtil.empty();

        // 汇总重量/体积
        var totalWeight = dispatchList.stream().mapToDouble(DispatchMsgDTO::getTotalWeight).sum();
        var totalVolume = dispatchList.stream().mapToDouble(DispatchMsgDTO::getTotalVolume).sum();

        // 构造消息
        var msg = Map.of(
                "truckId", truckPlanDto.getTruckId(),
                "driverIds", driverIds,
                "truckPlanId", truckPlanDto.getId(),
                "transportTripsId", truckPlanDto.getTransportTripsId(),
                "startOrganId", startOrganId,
                "endOrganId", endOrganId,
                "transportOrderIdList", orderIds,
                "totalWeight", totalWeight,
                "totalVolume", totalVolume,
                "dispatchDecisionId", dispatchDecisionId // 将决策ID回填到运输任务重，与业务数据关联
        );

        // 发送MQ
        this.mqFeign.sendMsg(
                Constants.MQ.Exchanges.TRANSPORT_TASK,
                Constants.MQ.RoutingKeys.TRANSPORT_TASK_CREATE,
                JSONUtil.toJsonStr(msg)
        );

        // 删除已分配的运单（Set结构去重用）
        if (CollUtil.isNotEmpty(orderIds)) {
            String setKey = this.transportOrderDispatchMQListener.getSetRedisKey(startOrganId, endOrganId);
            stringRedisTemplate.opsForSet().remove(setKey, orderIds.toArray());
        }
    }

    /**
     * 标记车辆计划完成
     */
    private void completeTruckPlan(List<TruckPlanDTO> list) {
        var msg = Map.of(
                "ids", CollStreamUtil.toList(list, TruckPlanDTO::getId),
                "created", System.currentTimeMillis()
        );

        mqFeign.sendMsg(
                Constants.MQ.Exchanges.TRUCK_PLAN,
                Constants.MQ.RoutingKeys.TRUCK_PLAN_COMPLETE,
                JSONUtil.toJsonStr(msg)
        );
    }

    /**
     * 发送训练数据到AIGC
     */
    private void sendTrainData(DispatchDecisionRecord record) {
        var msg = JSONUtil.toJsonStr(record);
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.AIGC, Constants.MQ.RoutingKeys.CREATE, msg);
    }

    /**
     * 构建调度决策记录（用于机器学习模型训练和决策分析）
     * <p>
     * 该方法创建一个完整的调度决策记录，包含：
     * 1. 车辆基本信息（载重、容积等）
     * 2. 调度特征数据（装载率、订单数量等）
     * 3. 调度结果（是否满载等）
     * 4. 决策状态和时间戳
     *
     * @param truck   车辆信息，包含载重、容积等基础属性
     * @param context 调度上下文，包含已选择的订单、跳过的订单数量等调度过程信息
     * @return DispatchDecisionRecord 完整的调度决策记录，用于后续的机器学习模型训练和决策分析
     */
    private DispatchDecisionRecord buildDecisionRecord(TruckDTO truck, DispatchContext context) {
        var record = new DispatchDecisionRecord();

        // 生成唯一的调度决策ID
        record.setDispatchDecisionId(IdUtil.simpleUUID());

        // 构建车辆信息（注意单位转换：吨->千克）
        var truckInfo = TruckInfo.builder()
                .maxWeight(truck.getAllowableLoad().doubleValue() * 1000)
                .maxVolume(truck.getAllowableVolume().doubleValue())
                .build();
        record.setTruck(truckInfo);

        // 构建特征快照（装载率、订单统计等关键特征）
        record.setFeatures(this.buildFeatures(truck, context));

        // 计算实际装载重量
        double usedWeight = context.getSelectedOrders()
                .stream().mapToDouble(DispatchMsgDTO::getTotalWeight).sum();

        // 判断是否达到满载标准（装载率>85%视为满载）
        var result = DispatchResult.builder()
                .isFullLoaded(usedWeight / truckInfo.getMaxWeight() > 0.85)
                .build();

        record.setResult(result);

        // 设置初始状态为待处理
        record.setStatus(DispatchStatus.PENDING);

        // 设置创建时间戳
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        return record;
    }

    /**
     * 构建调度特征快照（机器学习模型输入特征）
     * <p>
     * 该方法从调度结果中提取关键特征，用于：
     * 1. 机器学习模型训练和预测
     * 2. 调度效果评估和优化
     * 3. 决策质量分析
     * <p>
     * 特征包括：
     * - 装载特征：重量/体积利用率
     * - 订单特征：数量、平均重量、最大订单占比
     * - 调度过程特征：跳过订单数、剩余订单数、调度轮次
     *
     * @param truck   车辆信息，用于计算最大载重和容积
     * @param context 调度上下文，包含调度过程中的各种统计数据
     * @return FeatureSnapshot 特征快照，包含所有用于机器学习模型的特征数据
     */
    private FeatureSnapshot buildFeatures(TruckDTO truck, DispatchContext context) {
        var feature = new FeatureSnapshot();

        var orders = context.getSelectedOrders();

        // 计算总体装载情况
        double totalWeight = orders.stream().mapToDouble(DispatchMsgDTO::getTotalWeight).sum();
        double totalVolume = orders.stream().mapToDouble(DispatchMsgDTO::getTotalVolume).sum();

        // 车辆最大承载能力（注意单位转换：吨->千克）
        double maxWeight = truck.getAllowableLoad().doubleValue() * 1000;
        double maxVolume = truck.getAllowableVolume().doubleValue();

        // 装载率特征（核心指标，反映车辆利用率）
        feature.setUsedWeightRatio(totalWeight / maxWeight);
        feature.setUsedVolumeRatio(totalVolume / maxVolume);

        // 订单数量特征
        feature.setOrderCount(orders.size());

        // 订单重量统计特征
        feature.setAvgWeight(orders.stream().mapToDouble(DispatchMsgDTO::getTotalWeight).average().orElse(0));

        // 重量标准差（预留字段，可用于分析订单重量分布）
        feature.setStdWeight(0.0); // 可后续优化

        // 最大订单重量占比（反映是否有超大订单影响调度）
        var maxOrderWeightRatio = orders.stream().mapToDouble(DispatchMsgDTO::getTotalWeight).max().orElse(0) / maxWeight;
        feature.setMaxOrderWeightRatio(maxOrderWeightRatio);

        // 调度过程特征
        feature.setSkippedOrderCount(context.getSkippedCount()); // 因容量不足跳过的订单数
        feature.setRemainingOrderCount(context.getRemainingCount()); // 队列中剩余订单数
        feature.setRemainingHighPriorityRatio(context.getRemainingHighPriorityRatio()); // 剩余高优先级订单比例
        feature.setRounds(context.getRounds()); // 调度轮次（反映调度效率）

        return feature;
    }
}
