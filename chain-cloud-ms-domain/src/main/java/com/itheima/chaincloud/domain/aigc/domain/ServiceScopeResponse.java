package com.itheima.chaincloud.domain.aigc.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 服务范围分析响应结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceScopeResponse {

    @ApiModelProperty(value = "请求ID", required = true, example = "11")
    private String requestId;

    @ApiModelProperty(value = "推荐站点信息", required = true)
    private Map<String, Object> recommendedStation;

    @ApiModelProperty(value = "候选站点排名列表", required = true)
    private List<Map<String, Object>> candidateStations;

    @ApiModelProperty(value = "决策置信度 (0.00-1.00)", required = true, example = "0.7")
    private Double decisionConfidence;

    @ApiModelProperty(value = "是否需要人工审核", required = true, example = "false")
    private Boolean manualReview;

    @ApiModelProperty(value = "决策原因代码列表", required = true, example = "[\"IN_SERVICE_RANGE\", \"BETTER_LOAD_STATUS\"]")
    private List<String> reasonCodes;

    @ApiModelProperty(value = "推荐文本说明", required = true)
    private String recommendationText;

    @ApiModelProperty(value = "风险摘要", required = true)
    private String riskSummary;

    @ApiModelProperty(value = "详细原因列表", required = true)
    private List<String> reasons;

    @ApiModelProperty(value = "错误信息列表", required = true)
    private List<String> errors;
}
