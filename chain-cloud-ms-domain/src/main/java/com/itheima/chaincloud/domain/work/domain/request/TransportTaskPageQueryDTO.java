package com.itheima.chaincloud.domain.work.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportTaskPageQueryDTO {
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "运输计划id")
    private Long truckPlanId;
    @ApiModelProperty(value = "车次id")
    private Long transportTripsId;
    @ApiModelProperty(value = "起始机构id")
    private Long startAgencyId;
    @ApiModelProperty(value = "目的机构id")
    private Long endAgencyId;
    @ApiModelProperty(value = "状态")
    private Integer status;
    @ApiModelProperty(value = "分配状态")
    private Integer assignedStatus;
    @ApiModelProperty(value = "装载状态")
    private Integer loadingStatus;
    @ApiModelProperty(value = "车辆id")
    private Long truckId;
    @ApiModelProperty(value = "车辆id列表")
    private List<Long> truckIds;
    @ApiModelProperty(value = "计划发车时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planDepartureTime;
    @ApiModelProperty(value = "实际发车时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualDepartureTime;
    @ApiModelProperty(value = "计划到达时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planArrivalTime;
    @ApiModelProperty(value = "实际到达时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualArrivalTime;
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @ApiModelProperty(value = "页码", example = "1")
    private Integer page;
    @ApiModelProperty(value = "页尺寸", example = "10")
    private Integer pageSize;
}
