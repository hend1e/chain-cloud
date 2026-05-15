package com.itheima.chaincloud.domain.base.truck;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 回车登记列表元素
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckReturnRegisterListDTO {

    /**
     * 回车登记id
     */
    private Long id;

    /**
     * 车辆id
     */
    private Long truckId;

    /**
     * 车牌号码
     */
    private String licensePlate;

    /**
     * 运输任务id
     */
    private Long transportTaskId;

    /**
     * 出车时间
     */
    private LocalDateTime outStorageTime;

    /**
     * 回车时间
     */
    private LocalDateTime intoStorageTime;

    /**
     * 车辆是否可用
     */
    private Boolean isAvailable;
}