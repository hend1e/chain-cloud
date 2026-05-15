package com.itheima.chaincloud.domain.base.truck;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车次
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckTripsDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 车次名称
     */
    private String name;

    /**
     * 发车时间
     */
    private Integer departureTime;

    /**
     * 持续时间
     */
    private Integer estimatedTime;

    /**
     * 所属线路id
     */
    private Long transportLineId;

    /**
     * 周期，1为天，2为周，3为月
     */
    private Integer period;

    /**
     * 状态  0：禁用   1：正常
     */
    private Integer status;
}