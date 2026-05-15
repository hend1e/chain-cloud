package com.itheima.chaincloud.domain.transport.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运输路线搜索条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("运输路线搜索条件")
public class TransportLineSearchDTO {

    @ApiModelProperty(value = "路线名称")
    private String name;

    @ApiModelProperty(value = "路线编号")
    private String number;

    @ApiModelProperty(value = "起点机构id")
    private Long startOrganId;

    @ApiModelProperty(value = "终点机构id")
    private Long endOrganId;

    @ApiModelProperty(value = "页码")
    private Integer page = 1;

    @ApiModelProperty(value = "每页大小")
    private Integer pageSize = 10;
}