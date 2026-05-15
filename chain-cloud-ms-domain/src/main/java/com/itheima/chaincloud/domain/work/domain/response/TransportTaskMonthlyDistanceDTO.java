package com.itheima.chaincloud.domain.work.domain.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportTaskMonthlyDistanceDTO {
    @ApiModelProperty(value = "日期")
    private LocalDateTime dateTime;
    @ApiModelProperty(value = "里程")
    private Double mileage;
}
