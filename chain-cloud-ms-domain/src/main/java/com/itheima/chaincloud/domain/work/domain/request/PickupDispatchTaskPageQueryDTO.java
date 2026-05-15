package com.itheima.chaincloud.domain.work.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskAssignedStatusEnum;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskIsDeletedEnum;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskSignStatusEnum;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskStatusEnum;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("取派件任务分页查询")
public class PickupDispatchTaskPageQueryDTO {
    @ApiModelProperty(value = "页码", required = true, example = "1")
    private Integer page;
    @ApiModelProperty(value = "页尺寸", required = true, example = "10")
    private Integer pageSize;
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "订单id")
    private Long orderId;
    @ApiModelProperty(value = "网点id")
    private Long agencyId;
    @ApiModelProperty(value = "快递员id")
    private Long courierId;
    @ApiModelProperty(value = "任务类型")
    private PickupDispatchTaskTypeEnum taskType;
    @ApiModelProperty(value = "任务状态")
    private PickupDispatchTaskStatusEnum status;
    @ApiModelProperty(value = "分配状态")
    private PickupDispatchTaskAssignedStatusEnum assignedStatus;
    @ApiModelProperty(value = "签收状态")
    private PickupDispatchTaskSignStatusEnum signStatus;
    @ApiModelProperty(value = "最小预计完成时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime minEstimatedEndTime;
    @ApiModelProperty(value = "最大预计完成时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime maxEstimatedEndTime;
    @ApiModelProperty(value = "最小实际完成时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime minActualEndTime;
    @ApiModelProperty(value = "最大实际完成时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime maxActualEndTime;
    @ApiModelProperty(value = "删除状态")
    private PickupDispatchTaskIsDeletedEnum isDeleted;
}
