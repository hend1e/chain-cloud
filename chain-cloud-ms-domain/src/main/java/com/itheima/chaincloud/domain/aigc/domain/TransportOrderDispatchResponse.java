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
public class TransportOrderDispatchResponse {

    @ApiModelProperty(value = "优先级评分", required = true)
    private Double score;

    @ApiModelProperty(value = "评分原因", required = true)
    private String reason;
}
