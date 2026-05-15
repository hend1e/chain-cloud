package com.itheima.ms.web.customer.vo.carriage;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarriageComputeResponseVO {

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
