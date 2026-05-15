package com.itheima.ms.base.service.base.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.auth.factory.AuthTemplateFactory;
import com.itheima.auth.sdk.AuthTemplate;
import com.itheima.auth.sdk.common.Result;
import com.itheima.auth.sdk.dto.LoginDTO;
import com.itheima.auth.sdk.dto.UserDTO;
import com.itheima.ms.base.service.base.AuthService;
import com.itheima.chaincloud.common.exception.CCException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 权限系统对接服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    @Value("${cc.auth.account}")
    private String account;
    @Value("${cc.auth.password}")
    private String password;
    @Resource
    private AuthTemplate authTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 根据用户id获得详细信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    @Override
    public UserDTO getByUserId(Long id) {
        String redisKey = "AUTHORIZATION";
        String token = stringRedisTemplate.opsForValue().get(redisKey);

        if (ObjectUtil.isEmpty(token)) {
            Result<LoginDTO> loginDTO = authTemplate.opsForLogin().token(account, password);
            if (ObjectUtil.notEqual(loginDTO.getCode(), 0)) {
                String errorMsg = CharSequenceUtil.format("登录失败，账号：{}，密码：{}", account, password);
                throw new CCException(errorMsg);
            }

            token = loginDTO.getData().getToken().getToken();
            stringRedisTemplate.opsForValue().set(redisKey, token, 1, TimeUnit.HOURS);
            log.info("登录结果：{}", JSONUtil.toJsonStr(loginDTO));
        }

        authTemplate = AuthTemplateFactory.get(token);
        Result<UserDTO> result = authTemplate.opsForUser().getUserById(id);
        return result.getData();
    }
}