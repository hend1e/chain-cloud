package com.itheima.ms.web.customer.service;


import com.itheima.chaincloud.domain.user.domain.MemberDTO;
import com.itheima.ms.web.customer.bean.LoginInfo;
import com.itheima.ms.web.customer.vo.user.MemberVO;
import com.itheima.ms.web.customer.vo.user.RealNameVerifyVO;
import com.itheima.ms.web.customer.vo.user.UserLoginRequestVO;
import com.itheima.ms.web.customer.vo.user.UserLoginVO;

import java.io.IOException;
import java.util.Map;

/**
 * 用户管理
 */
public interface MemberService {

    String REDIS_LOGIN_INFO = "CC_CUSTOMER_LOGIN_INFO_";

    /**
     * 详情
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    MemberVO detail(Long userId);

    /**
     * 根据 openid 查询登录信息
     *
     * @param userId 用户id
     * @return 登录信息对象
     */
    LoginInfo getLoginInfo(Long userId);

    /**
     * 更新登录信息：更新刷新时间和刷新次数 +1
     *
     * @param userId 用户id
     */
    void updateLoginInfo(Long userId);

    /**
     * 新增
     *
     * @param user 用户信息
     */
    void save(MemberDTO user);

    /**
     * 根据openid查询用户
     *
     * @param openid 微信ID
     * @return 用户信息
     */
    MemberDTO getByOpenid(String openid);

    /**
     * 登录
     *
     * @param userLoginRequestVO 登录code
     * @return 用户信息
     * @throws IOException IO异常
     */
    UserLoginVO login(UserLoginRequestVO userLoginRequestVO) throws IOException;

    /**
     * 实名认证
     *
     * @param verifyVo 身份证号 姓名
     * @return 是否通过认证
     */
    RealNameVerifyVO realNameVerify(RealNameVerifyVO verifyVo);

    /**
     * 删除用户
     */
    void del();

    /**
     * 更新用户
     *
     * @param vo 用户
     */
    void update(MemberVO vo);

    /**
     * 刷新token
     *
     * @param refreshToken 原长令牌
     * @return 长/短令牌
     */
    UserLoginVO refresh(String refreshToken);
}
