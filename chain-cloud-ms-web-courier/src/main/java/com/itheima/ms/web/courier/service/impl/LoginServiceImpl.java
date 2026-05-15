package com.itheima.ms.web.courier.service.impl;

import com.itheima.chaincloud.api.auth.AuthFeign;
import com.itheima.chaincloud.common.util.HttpUtil;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.vo.R;
import com.itheima.ms.web.courier.service.LoginService;
import com.itheima.ms.web.courier.vo.login.AccountLoginVO;
import com.itheima.ms.web.courier.vo.login.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final AuthFeign authFeign;

    /**
     * 根据用户名和密码进行登录
     *
     * @param accountLoginVO 登录信息
     * @return token
     */
    @Override
    public R<LoginVO> accountLogin(AccountLoginVO accountLoginVO) {
        // 获取客户端 IP 和设备信息
        var clientIp = HttpUtil.getClientIp();
        var deviceInfo = HttpUtil.getDeviceInfo();

        // 调用auth服务进行登录
        var loginDTO = this.authFeign.login(accountLoginVO.getAccount(), accountLoginVO.getPassword(), 3, clientIp, deviceInfo);
        if (ObjectUtil.isEmpty(loginDTO)) {
            return R.error("登录失败");
        }
        return R.success(LoginVO.builder().token(loginDTO.getToken().getToken()).build());
    }
}
