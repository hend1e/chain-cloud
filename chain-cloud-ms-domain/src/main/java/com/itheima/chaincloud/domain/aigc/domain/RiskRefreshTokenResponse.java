package com.itheima.chaincloud.domain.aigc.domain;

import com.itheima.chaincloud.domain.aigc.enums.RiskLevel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskRefreshTokenResponse {

    @ApiModelProperty(value = "风险等级", required = true)
    private RiskLevel riskLevel;

    @ApiModelProperty(value = "风险分值", required = true)
    private Double riskScore;

    @ApiModelProperty(value = "原因", required = true)
    private String reason;
}
