package com.itheima.ms.auth.controller;

import com.itheima.auth.sdk.dto.LoginDTO;
import com.itheima.ms.auth.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@Api(tags = "鉴权服务")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     *
     * @param account    账号
     * @param password   密码
     * @param userType   用户类型
     * @param loginIp    登录 IP 地址
     * @param deviceInfo 设备信息
     * @return 登录信息
     */
    @PostMapping(value = "/login")
    @ApiOperation(value = "登录", notes = "登录")
    public LoginDTO login(@RequestParam("account") String account,
                          @RequestParam("password") String password,
                          @RequestParam("userType") Integer userType,
                          @RequestParam("loginIp") String loginIp,
                          @RequestParam("deviceInfo") String deviceInfo) {
        return this.authService.login(account, password, userType, loginIp, deviceInfo);
    }


}
