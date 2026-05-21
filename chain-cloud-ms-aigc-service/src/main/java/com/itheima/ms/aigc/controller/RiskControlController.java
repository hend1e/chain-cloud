package com.itheima.ms.aigc.controller;

import com.itheima.chaincloud.domain.aigc.domain.RiskLoginResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(tags = "AI 风控接口")
@RequestMapping("/aigc/risk")
@RequiredArgsConstructor
public class RiskControlController {

    /**
     * 登录账号的风险评估
     *
     * @param account    评估账号
     * @param userType   用户类型，1-管理员 2-快递员 3-司机 4-用户
     * @param loginIp    登录 IP 地址
     * @param deviceInfo 设备信息
     * @return 风险评估结果
     */
    @PostMapping("/login")
    @ApiOperation("登录风险评估")
    public RiskLoginResponse assessLoginRisk(@RequestParam("account") String account,
                                             @RequestParam("userType") Integer userType,
                                             @RequestParam("loginIp") String loginIp,
                                             @RequestParam("deviceInfo") String deviceInfo){
        return null;
    }

}
