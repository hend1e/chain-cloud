package com.itheima.ms.dispatch.ml.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车辆资源信息
 * <p>
 * 用于描述本次调度的"容量约束"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckInfo {

    /**
     * 最大载重（kg）
     */
    private Double maxWeight;

    /**
     * 最大体积（m³）
     */
    private Double maxVolume;
}
