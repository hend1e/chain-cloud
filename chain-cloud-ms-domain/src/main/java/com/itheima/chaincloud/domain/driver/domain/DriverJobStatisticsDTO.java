package com.itheima.chaincloud.domain.driver.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("任务数据统计")
public class DriverJobStatisticsDTO {
    @ApiModelProperty("任务数量,基于计划完成时间统计")
    private Integer taskAmounts;

    @ApiModelProperty("完成任务数量，基于实际完成时间统计")
    private Integer completedAmounts;

    @ApiModelProperty("运输里程，单位：公里，基于实际完成时间统计")
    private Long transportMileage;

    @ApiModelProperty("每日里程，基于实际完成时间统计")
    private List<DailyMileageDTO> dailyMileage;
}
