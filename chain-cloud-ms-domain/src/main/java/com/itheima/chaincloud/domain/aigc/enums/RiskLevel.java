package com.itheima.chaincloud.domain.aigc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RiskLevel {

    LOW("LOW", "正常"),
    MEDIUM("MEDIUM", "需要验证码"),
    HIGH("HIGH", "高风险");

    private final String code;
    private final String description;
}
