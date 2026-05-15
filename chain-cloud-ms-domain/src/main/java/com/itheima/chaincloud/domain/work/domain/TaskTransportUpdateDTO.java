package com.itheima.chaincloud.domain.work.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 手动调整DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTransportUpdateDTO {
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "车次Id")
    private Long transportTripsId;
    @ApiModelProperty(value = "车辆Id")
    private Long truckId;
    @ApiModelProperty(value = "司机Id")
    private List<Long> driverIds;
}
