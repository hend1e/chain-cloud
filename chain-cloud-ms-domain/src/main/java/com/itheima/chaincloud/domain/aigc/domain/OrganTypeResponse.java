package com.itheima.chaincloud.domain.aigc.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganTypeResponse {

    @ApiModelProperty(value = "机构类型", required = true, example = "AGENCY")
    private String organType;

    @ApiModelProperty(value = "置信度", required = true, example = "0.9")
    private Double confidence;

    @ApiModelProperty(value = "判断理由", required = true, example = "名称含直营网点，parent为分拣中心")
    private String reason;

    @ApiModelProperty(value = "是否建议审核", required = true, example = "false")
    private Boolean suggestReview;

    @ApiModelProperty(value = "标准化名称", required = true, example = "上海浦东直营网点")
    private String normalizedName;
}
