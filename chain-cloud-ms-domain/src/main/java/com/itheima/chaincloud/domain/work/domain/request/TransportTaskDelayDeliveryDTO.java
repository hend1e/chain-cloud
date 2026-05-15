package com.itheima.chaincloud.domain.work.domain.request;

import io.swagger.annotations.ApiModel;
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
@ApiModel("延迟提货对象")
public class TransportTaskDelayDeliveryDTO {
    @NotNull(message = "运输任务id不能为空")
    private String transportTaskId;
    @NotNull(message = "延迟时间不能为空")
    private String delayTime;
    @NotNull(message = "延迟原因不能为空")
    private String delayReason;
}
