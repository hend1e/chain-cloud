package com.itheima.gateway.filter;

import cn.hutool.core.map.MapUtil;
import com.itheima.auth.sdk.dto.AuthUserInfoDTO;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.util.JwtUtils;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.gateway.config.MyConfig;
import com.itheima.gateway.properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 快递员token拦截处理
 */
@Slf4j
@Component
public class CustomerTokenGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> implements AuthFilter {

    @Override
    public GatewayFilter apply(Object config) {
        return null;
    }

    @Override
    public AuthUserInfoDTO check(String token) {
        return null;
    }

    @Override
    public Boolean auth(String token, AuthUserInfoDTO authUserInfoDTO, String path) {
        return null;
    }

    @Override
    public String tokenHeaderName() {
        return null;
    }
}