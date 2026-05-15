package com.itheima.chaincloud.domain.transport.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 运输路线对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("运输路线")
public class TransportLineDTO {

    @ApiModelProperty(value = "路线id")
    private Long id;

    @ApiModelProperty(value = "路线编号")
    private String number;

    @ApiModelProperty(value = "成本")
    private Double cost;

    @Min(value = 1, message = "类型值必须是1、2、3")
    @Max(value = 3, message = "类型值必须是1、2、3")
    @ApiModelProperty(value = "类型：1-干线，2-支线，3-接驳路线", required = true)
    private Integer type;

    @ApiModelProperty(value = "路线名称")
    private String name;

    @ApiModelProperty(value = "距离（米）")
    private Double distance;

    @ApiModelProperty(value = "时间（秒）")
    private Double time;

    @ApiModelProperty(value = "创建时间")
    private Long created;

    @ApiModelProperty(value = "修改时间")
    private Long updated;

    @ApiModelProperty(value = "扩展字段（JSON格式）")
    private String extra;

    @ApiModelProperty(value = "起点机构id")
    private Long startOrganId;

    @ApiModelProperty(value = "起点机构名称")
    private String startOrganName;

    @ApiModelProperty(value = "终点机构id")
    private Long endOrganId;

    @ApiModelProperty(value = "终点机构名称")
    private String endOrganName;
}