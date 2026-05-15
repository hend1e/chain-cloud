package com.itheima.ms.web.driver.service.impl;

import com.itheima.chaincloud.api.auth.AuthFeign;
import com.itheima.ms.web.driver.service.LoginService;
import com.itheima.ms.web.driver.vo.request.AccountLoginVO;
import com.itheima.chaincloud.common.exception.CCWebException;
import com.itheima.chaincloud.common.util.HttpUtil;
import com.itheima.chaincloud.common.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final AuthFeign authFeign;

    @Override
    public String accountLogin(AccountLoginVO accountLoginVO) {

        // 获取客户端 IP 和设备信息
        var clientIp = HttpUtil.getClientIp();
        var deviceInfo = HttpUtil.getDeviceInfo();

        // 调用auth服务进行登录
        var loginDTO = this.authFeign.login(accountLoginVO.getAccount(), accountLoginVO.getPassword(), 3, clientIp, deviceInfo);

        if (ObjectUtil.isEmpty(loginDTO)) {
            throw new CCWebException("登录失败");
        }
        return loginDTO.getToken().getToken();
    }
}
