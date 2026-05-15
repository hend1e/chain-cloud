package com.itheima.chaincloud.domain.base.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 司机
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckDriverDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 车辆id
     */
    private Long truckId;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 照片
     */
    private String picture;

    /**
     * 驾龄
     */
    private Integer drivingAge;
}