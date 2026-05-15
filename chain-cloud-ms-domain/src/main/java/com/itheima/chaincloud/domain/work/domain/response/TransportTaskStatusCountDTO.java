package com.itheima.chaincloud.domain.work.domain.response;

import com.itheima.chaincloud.domain.work.enums.TransportTaskStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportTaskStatusCountDTO {
    @ApiModelProperty(value = "状态枚举", required = true)
    private TransportTaskStatusEnum status;
    @ApiModelProperty(value = "状态编码", required = true)
    private Integer statusCode;
    @ApiModelProperty(value = "数量", required = true)
    private Long count;
}
