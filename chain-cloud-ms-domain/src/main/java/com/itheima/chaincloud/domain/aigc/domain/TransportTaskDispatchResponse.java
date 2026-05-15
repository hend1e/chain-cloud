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
public class TransportTaskDispatchResponse {

    @ApiModelProperty(value = "预测标签", required = true, example = "delayed")
    private String prediction_label;

    @ApiModelProperty(value = "置信度", required = true, example = "0.8925")
    private Double confidence;

    @ApiModelProperty(value = "预测原因", required = true)
    private String reason;
}
