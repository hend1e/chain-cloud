package com.itheima.ms.web.driver.service.impl;

import com.itheima.ms.web.driver.service.LoginService;
import com.itheima.ms.web.driver.vo.request.AccountLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    @Override
    public String accountLogin(AccountLoginVO accountLoginVO) {
        return null;
    }
}
