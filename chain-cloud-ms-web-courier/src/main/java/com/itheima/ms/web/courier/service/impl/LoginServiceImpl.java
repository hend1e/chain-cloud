package com.itheima.ms.web.courier.service.impl;

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

    /**
     * 根据用户名和密码进行登录
     *
     * @param accountLoginVO 登录信息
     * @return token
     */
    @Override
    public R<LoginVO> accountLogin(AccountLoginVO accountLoginVO) {
        return null;
    }
}
