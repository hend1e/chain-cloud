package com.itheima.ms.auth.controller;

import com.itheima.chaincloud.domain.auth.domain.LoginRecordDTO;
import com.itheima.ms.auth.service.LoginRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@Api(tags = "登录记录管理")
@RequestMapping("/auth/login/record")
@RequiredArgsConstructor
public class LoginRecordController {

    private final LoginRecordService loginRecordService;

    @GetMapping
    @ApiOperation(value = "查询登录记录（最近10条）")
    public List<LoginRecordDTO> queryRecordList(@RequestParam("account") String account, @RequestParam("userType") Integer userType) {
        return this.loginRecordService.queryRecordList(account, userType);
    }
}
