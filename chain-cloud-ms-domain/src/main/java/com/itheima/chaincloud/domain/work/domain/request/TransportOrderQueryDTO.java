package com.itheima.chaincloud.domain.work.domain.request;

import com.itheima.chaincloud.domain.work.enums.TransportOrderSchedulingStatusEnum;
import com.itheima.chaincloud.domain.work.enums.TransportOrderStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportOrderQueryDTO {
    @ApiModelProperty(value = "运单id")
    private String id;
    @ApiModelProperty(value = "订单id")
    private Long orderId;
    @ApiModelProperty(value = "运单状态")
    private TransportOrderStatusEnum status;
    @ApiModelProperty(value = "调度状态")
    private TransportOrderSchedulingStatusEnum schedulingStatus;
    @ApiModelProperty(value = "起始机构id")
    private Long startAgencyId;
    @ApiModelProperty(value = "结束机构id")
    private Long endAgencyId;
    @ApiModelProperty(value = "当前机构id")
    private Long currentAgencyId;
    @ApiModelProperty(value = "页码")
    private Integer page;
    @ApiModelProperty(value = "页尺寸")
    private Integer pageSize;
}
