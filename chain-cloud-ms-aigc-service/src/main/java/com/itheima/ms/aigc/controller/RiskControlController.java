package com.itheima.ms.aigc.controller;

import com.itheima.ms.aigc.service.RiskControlService;
import com.itheima.chaincloud.domain.aigc.domain.RiskLoginResponse;
import com.itheima.chaincloud.domain.aigc.domain.RiskRefreshTokenResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(tags = "AI 风控接口")
@RequestMapping("/aigc/risk")
@RequiredArgsConstructor
public class RiskControlController {

    private final RiskControlService riskControlService;

    /**
     * 登录风险评估
     *
     * @param account  评估账号
     * @param userType 用户类型，1-管理员 2-快递员 3-司机 4-用户
     * @return 风险评估结果
     */
    @PostMapping("/login")
    @ApiOperation("登录风险评估")
    public RiskLoginResponse assessLoginRisk(@RequestParam("account") String account, @RequestParam("userType") Integer userType) {
        return riskControlService.assessLoginRisk(account, userType);
    }

    /**
     * refreshToken风险评估
     *
     * @param body  评估的信息
     * @return 风险评估结果
     */
    @ApiOperation("评估刷新令牌的风险")
    @PostMapping("/refreshToken")
    public RiskRefreshTokenResponse assessRefreshTokenRisk(@RequestBody String body) {
        return riskControlService.assessRefreshTokenRisk(body);
    }
}
