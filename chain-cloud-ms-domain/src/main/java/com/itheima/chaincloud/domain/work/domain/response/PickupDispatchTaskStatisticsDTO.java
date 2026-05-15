package com.itheima.chaincloud.domain.work.domain.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("取派件任务统计")
public class PickupDispatchTaskStatisticsDTO {
    @ApiModelProperty("取件任务总数")
    private Integer pickupNum = 0;
    @ApiModelProperty("新增取件数")
    private Integer newPickUpNum = 0;
    @ApiModelProperty("已完成取件数")
    private Integer completePickUpNum = 0;
    @ApiModelProperty("已取消取件数")
    private Integer cancelPickUpNum = 0;
    @ApiModelProperty("派件任务总数")
    private Integer dispatchNum = 0;
    @ApiModelProperty("新增派件数")
    private Integer newDispatchNum = 0;
    @ApiModelProperty("已签收数")
    private Integer signedNum = 0;
    @ApiModelProperty("已取消派件数")
    private Integer cancelDispatchNum = 0;
}
