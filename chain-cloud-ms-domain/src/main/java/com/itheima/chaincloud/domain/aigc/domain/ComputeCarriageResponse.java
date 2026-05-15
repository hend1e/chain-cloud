package com.itheima.chaincloud.domain.aigc.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComputeCarriageResponse {

    @ApiModelProperty(value = "计费重量", required = true)
    private Double computeWeight;

    @ApiModelProperty(value = "续重单价", required = true)
    private Double continuousWeight;

    @ApiModelProperty(value = "总运费", required = true)
    private Double expense;

    @ApiModelProperty(value = "首重价格", required = true)
    private Double firstWeight;

    @ApiModelProperty(value = "轻抛系数", required = true)
    private Integer lightThrowingCoefficient;

    @ApiModelProperty(value = "运费计算说明", required = true)
    private String info;
}
