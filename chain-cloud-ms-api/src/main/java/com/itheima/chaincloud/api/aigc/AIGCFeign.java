package com.itheima.chaincloud.api.aigc;

import com.itheima.chaincloud.domain.aigc.domain.RiskLoginResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@FeignClient(name = "chain-cloud-ms-aigc", contextId = "AIGC", path = "/aigc")
public interface AIGCFeign {

    /**
     * 登录账号的风险评估
     *
     * @param account  评估账号
     * @param userType 用户类型，1-管理员 2-快递员 3-司机 4-用户
     * @return 风险评估结果
     */
    @PostMapping("/risk/login")
    RiskLoginResponse assessLoginRisk(@RequestParam("account") String account, @RequestParam("userType") Integer userType);

}
