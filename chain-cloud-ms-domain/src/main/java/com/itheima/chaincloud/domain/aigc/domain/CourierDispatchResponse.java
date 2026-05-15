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
public class CourierDispatchResponse {

    @ApiModelProperty(value = "选中的快递员ID", required = true)
    private String courierId;

    @ApiModelProperty(value = "选择原因", required = true)
    private String reason;
}
