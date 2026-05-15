package com.itheima.chaincloud.domain.aigc.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierDispatchRequest {

    @ApiModelProperty(value = "快递员ID列表", required = true)
    private List<Long> courierIds;

    @ApiModelProperty(value = "经度", required = true)
    private Double longitude;

    @ApiModelProperty(value = "纬度", required = true)
    private Double latitude;
}
