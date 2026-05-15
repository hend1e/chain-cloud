package com.itheima.ms.dispatch.ml.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调度结果（用于生成标签 label）
 * <p>
 * ⚠️ 注意：
 * 这些字段来自"业务结果"，不是调度时计算的
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchResult {

    /**
     * 是否接近满载（调度即时可计算）
     */
    private Boolean isFullLoaded;

    /**
     * 是否延误（需要运输完成后回填）
     */
    private Boolean isDelayed;

    /**
     * 是否人工干预（后置结果）
     */
    private Boolean manualAdjusted;
}
