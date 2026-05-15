package com.itheima.chaincloud.domain.driver.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverDelayDeliveryDTO {
    @NotNull(message = "司机作业单id不能为空")
    @ApiModelProperty(value = "司机作业单id", required = true)
    private String id;

    @NotNull(message = "延迟时间不能为空")
    @ApiModelProperty(value = "延迟时间", required = true, example = "2022-07-18 15:20:00")
    private String delayTime;

    @NotNull(message = "延迟原因不能为空")
    @ApiModelProperty(value = "延迟原因", required = true)
    private String delayReason;
}
