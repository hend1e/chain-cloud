package com.itheima.gateway.filter;

import com.itheima.auth.sdk.dto.AuthUserInfoDTO;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * 管理端token校验的过滤器
 */
@Component
public class ManagerTokenGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> implements AuthFilter {

    @Override
    public GatewayFilter apply(Object config) {
        return null;
    }

    @Override
    public AuthUserInfoDTO check(String token) {
        return null;
    }

    @Override
    public Boolean auth(String token, AuthUserInfoDTO authUserInfo, String path) {
        return false;
    }
}
