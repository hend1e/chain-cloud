package com.itheima.ms.dispatch.ml.model;

import com.itheima.ms.dispatch.ml.model.enums.DispatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 调度决策记录（用于AI训练的数据闭环核心对象）
 * <p>
 * 一条记录代表：
 * 👉 一次"车辆调度决策" + 后续业务结果（回填）
 * <p>
 * 用途：
 * 1. 训练机器学习模型（XGBoost等）
 * 2. 调度策略评估
 * 3. 数据分析
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchDecisionRecord {

    /**
     * 业务唯一ID（必须）
     * 👉 用于回填（调度 → 运输完成 → 更新结果）
     */
    private String dispatchDecisionId;

    /**
     * 车辆信息（资源约束）
     */
    private TruckInfo truck;

    /**
     * 特征数据（模型输入 X）
     */
    private FeatureSnapshot features;

    /**
     * 结果数据（用于生成 label）
     */
    private DispatchResult result;

    /**
     * 状态：
     * PENDING：未回填
     * COMPLETED：已回填
     */
    private DispatchStatus status;

    /**
     * 创建时间（调度发生时间）
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间（回填时间）
     */
    private LocalDateTime updatedAt;
}
