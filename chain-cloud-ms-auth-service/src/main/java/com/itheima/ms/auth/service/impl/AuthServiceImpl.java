package com.itheima.ms.auth.service.impl;

import com.itheima.auth.sdk.AuthTemplate;
import com.itheima.auth.sdk.dto.LoginDTO;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.ms.auth.service.AuthService;
import com.itheima.ms.auth.service.LoginRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthTemplate authTemplate;
    private final LoginRecordService loginRecordService;

    @Override
    public LoginDTO login(String account, String password) {
        var result = this.authTemplate.opsForLogin().token(account, password);
        if (result.getCode() == 0) {
            // 登录成功
            return result.getData();
        }
        return null;
    }

    @Override
    public LoginDTO login(String account, String password, Integer userType,
                          String loginIp, String deviceInfo) {
        var result = this.login(account, password);
        if (ObjectUtil.isNotEmpty(result)) {
            // 登录成功，保存登录记录
            loginRecordService.saveLoginRecord(account, userType, loginIp, deviceInfo, 1, null);
            // 登录成功
            return result;
        }
        // 登录失败，保存失败记录
        loginRecordService.saveLoginRecord(account, userType, loginIp, deviceInfo, 0, "用户名或密码错误");
        return null;
    }
}
