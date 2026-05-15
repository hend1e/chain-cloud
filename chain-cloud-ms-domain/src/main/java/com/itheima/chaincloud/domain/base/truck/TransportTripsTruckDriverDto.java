package com.itheima.chaincloud.domain.base.truck;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车次车辆司机关联
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportTripsTruckDriverDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 车辆id
     */
    private Long truckId;

    /**
     * 车次id
     */
    private Long transportTripsId;

    /**
     * 司机Id
     */
    private Long driverId;
}