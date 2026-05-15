package com.itheima.gateway.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.auth.sdk.dto.AuthUserInfoDTO;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.gateway.config.MyConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class TokenGatewayFilter implements GatewayFilter, Ordered {

    private AuthFilter authFilter;
    private MyConfig myConfig;

    public TokenGatewayFilter(MyConfig myConfig, AuthFilter authFilter) {
        this.myConfig = myConfig;
        this.authFilter = authFilter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1. 校验请求路径，如果是白名单，直接放行
        String path = exchange.getRequest().getPath().toString();
        if (StrUtil.startWithAny(path, this.myConfig.getNoAuthPaths())) {
            //直接放行
            return chain.filter(exchange);
        }

        //2. 获取请求头中的token，进行校验，如果为空或校验失效，响应401
        String token = exchange.getRequest().getHeaders().getFirst(this.authFilter.tokenHeaderName());
        if (StrUtil.isEmpty(token)) {
            //设置响应状态为401
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            //拦截请求
            return exchange.getResponse().setComplete();
        }
        //校验token
        AuthUserInfoDTO authUserInfoDTO = null;
        try {
            authUserInfoDTO = this.authFilter.check(token);
        } catch (Exception e) {
            //token不可用，不做处理
        }

        if (ObjectUtil.isEmpty(authUserInfoDTO)) {
            //token不可用，设置响应状态为401
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            //拦截请求
            return exchange.getResponse().setComplete();
        }

        //3. 校验权限
        Boolean auth = false;
        try {
            auth = this.authFilter.auth(token, authUserInfoDTO, path);
        } catch (Exception e) {
            log.error("鉴权失败, token = {}", token, e);
        }
        if (!auth) {
            //没有权限，设置响应状态码为400
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        //4. 校验通过，向下游传递用户信息和token
        exchange.getRequest().mutate().header(Constants.GATEWAY.USERINFO, JSONUtil.toJsonStr(authUserInfoDTO));
        exchange.getRequest().mutate().header(Constants.GATEWAY.TOKEN, token);
        //4.1 校验通过放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
