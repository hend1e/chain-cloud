package com.itheima.ms.auth.service;

import com.itheima.auth.sdk.dto.LoginDTO;

/**
 * 鉴权服务接口
 */
public interface AuthService {

    /**
     * 登录
     *
     * @param account  账号
     * @param password 密码
     * @return 登录信息
     */
   LoginDTO login(String account, String password);
    
    /**
     * 登录（带登录记录保存）
     *
     * @param account     账号
     * @param password    密码
     * @param userType    用户类型
     * @param loginIp     登录 IP 地址
     * @param deviceInfo  设备信息
     * @return 登录信息
     */
   LoginDTO login(String account, String password, Integer userType, 
                  String loginIp, String deviceInfo);
}
