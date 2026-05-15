package com.itheima.chaincloud.domain.scope.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceScopeDTO {

    @ApiModelProperty(value = "业务id，可以是机构或快递员", required = true)
    @NotNull(message = "bid不能为空")
    private Long bid;

    @ApiModelProperty(value = "类型，1-机构，2-快递员", required = true)
    @NotNull(message = "type不能为空")
    private Integer type;

    @ApiModelProperty(value = "多边形坐标点，至少3个坐标点，首尾坐标必须相同", required = true)
    @Size(min = 3, message = "坐标列表至少3个坐标点")
    private List<Coordinate> polygon;

    @ApiModelProperty(value = "创建时间")
    private Long created;

    @ApiModelProperty(value = "更新时间")
    private Long updated;

}
