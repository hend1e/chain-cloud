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
public class RiskLoginResponse {

    @ApiModelProperty(value = "风险等级", required = true)
    private RiskLevel riskLevel;

    @ApiModelProperty(value = "是否需要验证码", required = true)
    private Boolean needCaptcha;

    @ApiModelProperty(value = "原因", required = true)
    private String reason;
}
