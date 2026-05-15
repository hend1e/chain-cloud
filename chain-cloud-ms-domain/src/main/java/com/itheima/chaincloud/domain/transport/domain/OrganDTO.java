package com.itheima.chaincloud.domain.transport.domain;

import cn.hutool.core.annotation.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 机构数据对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("机构信息")
public class OrganDTO {

    @Alias("bid")
    @NotNull(message = "机构id不能为空")
    @ApiModelProperty(value = "机构id", required = true)
    private Long id;

    @ApiModelProperty(value = "名称", required = true)
    private String name;

    @Min(value = 1, message = "类型值必须是1、2、3")
    @Max(value = 3, message = "类型值必须是1、2、3")
    @ApiModelProperty(value = "类型：1-一级转运，2-二级转运，3-网点", required = true)
    private Integer type;

    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "地址")
    private String address;

    @ApiModelProperty(value = "纬度")
    private Double latitude;

    @ApiModelProperty(value = "经度")
    private Double longitude;

    @ApiModelProperty(value = "父节点id")
    private Long parentId;

    @ApiModelProperty(value = "负责人")
    private String managerName;

    @ApiModelProperty(value = "扩展字段（JSON格式）")
    private String extra;

    @ApiModelProperty(value = "是否可用")
    private Boolean status;
}