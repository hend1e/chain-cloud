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
@ApiModel("调度配置")
public class DispatchConfigurationDTO {

    @NotNull(message = "调度时间不能为空！")
    @Min(value = 1, message = "调度时间最小为1！")
    @Max(value = 48, message = "调度时间最大为48！")
    @ApiModelProperty(value = "调度时间（小时），分配运输任务的提前量", required = true)
    private Integer dispatchTime;

    @NotNull(message = "调度方式不能为空！")
    @Min(value = 1, message = "调度方式最小为1！")
    @Max(value = 2, message = "调度方式最大为2！")
    @ApiModelProperty(value = "调度方式：1-转运次数最少，2-成本最低", required = true)
    private Integer dispatchMethod;
}