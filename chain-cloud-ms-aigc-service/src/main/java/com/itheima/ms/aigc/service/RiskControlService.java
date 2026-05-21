package com.itheima.ms.aigc.service;

import com.itheima.chaincloud.domain.aigc.domain.RiskLoginResponse;
import com.itheima.chaincloud.domain.aigc.domain.RiskRefreshTokenResponse;

public interface RiskControlService {

    /**
     * 登录风险评估
     *
     * @param account  评估账号
     * @param userType 用户类型，1-管理员 2-快递员 3-司机 4-用户
     * @param loginIp    登录 IP 地址
     * @param deviceInfo 设备信息
     * @return 风险评估结果
     */
    RiskLoginResponse assessLoginRisk(String account, Integer userType, String loginIp, String deviceInfo);

    /**
     * refreshToken风险评估
     *
     * @param body 评估的信息
     * @return 风险评估结果
     */
    RiskRefreshTokenResponse assessRefreshTokenRisk(String body);
}
