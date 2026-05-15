package com.itheima.ms.web.courier.service;


import com.itheima.ms.web.courier.vo.login.AccountLoginVO;
import com.itheima.ms.web.courier.vo.login.LoginVO;
import com.itheima.chaincloud.common.vo.R;

public interface LoginService {

    /**
     * 根据用户名和密码进行登录
     *
     * @param accountLoginVO 登录信息
     * @return token
     */
    R<LoginVO> accountLogin(AccountLoginVO accountLoginVO);
}
