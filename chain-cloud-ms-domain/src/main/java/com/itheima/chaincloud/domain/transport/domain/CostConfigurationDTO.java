package com.itheima.chaincloud.domain.transport.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("成本配置")
public class CostConfigurationDTO {

    @NotNull(message = "线路类型不能为空！")
    @Min(value = 1, message = "线路类型最小为1！")
    @Max(value = 3, message = "线路类型最大为3！")
    @ApiModelProperty(value = "线路类型：1-干线，2-支线，3-接驳路线", required = true)
    private Integer transportLineType;

    @NotNull(message = "成本不能为空！")
    @ApiModelProperty(value = "成本（元/公里），默认：干线0.8、支线1.2、接驳1.5", required = true)
    private Double cost;
}