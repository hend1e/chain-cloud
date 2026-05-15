package com.itheima.chaincloud.domain.transport.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 运输路线节点对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("运输路线节点")
public class TransportLineNodeDTO {

    @ApiModelProperty(value = "节点列表", required = true)
    private List<OrganDTO> nodeList;

    @ApiModelProperty(value = "路线成本")
    private Double cost;
}