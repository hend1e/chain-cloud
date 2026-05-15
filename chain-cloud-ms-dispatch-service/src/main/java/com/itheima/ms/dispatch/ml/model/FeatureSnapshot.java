package com.itheima.ms.dispatch.ml.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调度特征快照（模型输入）
 * <p>
 * 描述：调度当时的状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureSnapshot {

    /**
     * 已使用重量比例（0~1）
     */
    private Double usedWeightRatio;

    /**
     * 已使用体积比例（0~1）
     */
    private Double usedVolumeRatio;

    /**
     * 已分配订单数量
     */
    private Integer orderCount;

    /**
     * 平均订单重量
     */
    private Double avgWeight;

    /**
     * 订单重量标准差（反映分布是否均匀）
     */
    private Double stdWeight;

    /**
     * 最大订单占比（大单影响）
     */
    private Double maxOrderWeightRatio;

    /**
     * 被跳过的订单数（装不下）
     */
    private Integer skippedOrderCount;

    /**
     * 剩余未分配订单数
     */
    private Long remainingOrderCount;

    /**
     * 剩余高优订单比例（关键特征🔥）
     */
    private Double remainingHighPriorityRatio;

    /**
     * 调度循环轮数（算法复杂度）
     */
    private Integer rounds;
}
