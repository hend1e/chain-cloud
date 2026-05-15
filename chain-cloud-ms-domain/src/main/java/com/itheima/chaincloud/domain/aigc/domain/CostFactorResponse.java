package com.itheima.chaincloud.domain.aigc.domain;

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
public class CostFactorResponse {

    @ApiModelProperty(value = "区域关系修正因子", required = true, example = "1.05")
    private Double regionFactor;

    @ApiModelProperty(value = "距离分段修正因子", required = true, example = "1.02")
    private Double distanceSegmentFactor;

    @ApiModelProperty(value = "路径效率修正因子", required = true, example = "0.97")
    private Double efficiencyFactor;

    @ApiModelProperty(value = "收费道路修正因子", required = true, example = "1.04")
    private Double tollFactor;

    @ApiModelProperty(value = "绕行修正因子", required = true, example = "1.03")
    private Double detourFactor;

    @ApiModelProperty(value = "路径复杂度修正因子", required = true, example = "1.01")
    private Double routeComplexityFactor;

    @ApiModelProperty(value = "成熟通道修正因子", required = true, example = "0.99")
    private Double corridorFactor;

    @ApiModelProperty(value = "相似路线经验校准因子", required = true, example = "1.02")
    private Double similarityCalibrationFactor;

    @ApiModelProperty(value = "起终点接入难度修正因子", required = true, example = "1.01")
    private Double accessFactor;

    @ApiModelProperty(value = "置信度", required = true, example = "0.88")
    private Double confidence;

    @ApiModelProperty(value = "使用的工具列表", required = true, example = "[\"amap_driving_route_planning\"]")
    private List<String> usedTools;

    @ApiModelProperty(value = "判断理由", required = true, example = "跨省干线运输，距离较长，预计收费道路占比高")
    private String reason;
}
