package com.itheima.gateway.filter;

import cn.hutool.core.collection.CollUtil;
import com.itheima.auth.factory.AuthTemplateFactory;
import com.itheima.auth.sdk.AuthTemplate;
import com.itheima.auth.sdk.dto.AuthUserInfoDTO;
import com.itheima.auth.sdk.service.TokenCheckService;
import com.itheima.gateway.config.MyConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 管理端token校验的过滤器
 */
@Component
public class ManagerTokenGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> implements AuthFilter {

    @Resource
    private MyConfig myConfig;
    @Resource
    private TokenCheckService tokenCheckService;
    @Value("${role.manager}")
    private List<Long> managerRoleIds; //获取配置文件中的管理员角色id

    @Override
    public GatewayFilter apply(Object config) {
        //由于要传递当前对象，所以不能采用spring注入的方式，这里采用手动构造对象的方式传入
        return new TokenGatewayFilter(this.myConfig, this);
    }

    @Override
    public AuthUserInfoDTO check(String token) {
        try {
            return this.tokenCheckService.parserToken(token);
        } catch (Exception e) {
            //token不可用，不做处理
        }
        return null;
    }

    @Override
    public Boolean auth(String token, AuthUserInfoDTO authUserInfo, String path) {
        //校验权限，如果是非管理员不能登录
        AuthTemplate authTemplate = AuthTemplateFactory.get(token);
        //获取用户拥有的角色id列表
        List<Long> roleIds = authTemplate.opsForRole().findRoleByUserId(authUserInfo.getUserId()).getData();
        //取交集，判断用户拥有的角色是否与预定的角色列表是否有交集
        Collection<Long> intersection = CollUtil.intersection(roleIds, this.managerRoleIds);
        return CollUtil.isNotEmpty(intersection);
    }
}
