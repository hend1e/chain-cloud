package com.itheima.chaincloud.domain.driver.domain;

import io.swagger.annotations.ApiModel;
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
@ApiModel("每日里程")
public class DailyMileageDTO {
    @ApiModelProperty(value = "日期,格式：2022-07-16 00:00:00")
    private LocalDateTime dateTime;

    @ApiModelProperty(value = "里程，单位：公里;计算公式：原始数据（单位米）/1000 四舍五入取整")
    private Long mileage;
}
