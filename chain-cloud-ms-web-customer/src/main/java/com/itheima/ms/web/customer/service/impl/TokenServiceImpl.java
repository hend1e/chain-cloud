package com.itheima.ms.web.customer.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.api.aigc.AIGCFeign;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.exception.CCWebException;
import com.itheima.chaincloud.common.util.HttpUtil;
import com.itheima.chaincloud.common.util.JwtUtils;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.util.StrUtil;
import com.itheima.chaincloud.domain.aigc.enums.RiskLevel;
import com.itheima.ms.web.customer.properties.JwtProperties;
import com.itheima.ms.web.customer.service.MemberService;
import com.itheima.ms.web.customer.service.TokenService;
import com.itheima.ms.web.customer.vo.user.UserLoginVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService {

    public static final String REDIS_REFRESH_TOKEN_PREFIX = "CC_CUSTOMER_REFRESH_TOKEN_";
    @Resource
    private JwtProperties jwtProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MemberService memberService;
    @Resource
    private AIGCFeign aigcFeign;

    public String createAccessToken(Map<String, Object> claims) {
        //生成短令牌的有效期时间单位为：分钟
        return JwtUtils.createToken(claims, jwtProperties.getPrivateKey(), jwtProperties.getAccessTtl(), DateField.MINUTE);
    }

    @Override
    public String createRefreshToken(Map<String, Object> claims) {
        //生成长令牌的有效期时间单位为：小时
        Integer ttl = jwtProperties.getRefreshTtl();
        String refreshToken = JwtUtils.createToken(claims, jwtProperties.getPrivateKey(), ttl);

        //长令牌只能使用一次，需要将其存储到redis中，变成有状态的
        String redisKey = this.getRedisRefreshToken(refreshToken);
        this.stringRedisTemplate.opsForValue().set(redisKey, refreshToken, Duration.ofHours(ttl));
        return refreshToken;
    }

    private String getRedisRefreshToken(String refreshToken) {
        //md5是为了缩短key的长度
        return REDIS_REFRESH_TOKEN_PREFIX + SecureUtil.md5(refreshToken);
    }

    @Override
    public UserLoginVO refreshToken(String refreshToken) {
        if (StrUtil.isEmpty(refreshToken)) {
            return null;
        }

        Map<String, Object> originClaims = JwtUtils.checkToken(refreshToken, this.jwtProperties.getPublicKey());
        if (ObjectUtil.isEmpty(originClaims)) {
            //token无效
            return null;
        }

        //通过redis校验，原token是否使用过，来确保token只能使用一次
        String redisKey = this.getRedisRefreshToken(refreshToken);
        Boolean bool = this.stringRedisTemplate.hasKey(redisKey);
        if (ObjectUtil.notEqual(bool, Boolean.TRUE)) {
            //原token过期或已经使用过
            return null;
        }

        // 从claims中获取用户id
        var userId = MapUtil.getLong(originClaims, Constants.GATEWAY.USER_ID);

        // ★ 评估刷新token的风险
        if (this.checkRefreshTokenRisk(userId)) {
            throw new CCWebException("此次刷新token操作处于高风险等级，请重新登录");
        }

        // 更新用户登录信息
        this.memberService.updateLoginInfo(userId);

        //删除原token
        this.stringRedisTemplate.delete(redisKey);

        //重新生成长短令牌
        String newRefreshToken = this.createRefreshToken(originClaims);
        String accessToken = this.createAccessToken(originClaims);

        return UserLoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    private boolean checkRefreshTokenRisk(Long userId) {
        // 根据用户id获取用户登录信息
        var loginInfo = this.memberService.getLoginInfo(userId);

        // 补充当前刷新ip和设备信息
        var clientIp = HttpUtil.getClientIp();
        var deviceInfo = HttpUtil.getDeviceInfo();

        // 第一步：快速检查 - IP 和设备是否都没有变化
        if (StrUtil.equals(clientIp, loginInfo.getRefreshIP())
                && StrUtil.equals(deviceInfo, loginInfo.getRefreshDeviceInfo())) {
            // ip和设备信息都与登录时一致，属于安全情况
            return false;
        }

        // 第二步：快速检查 - IP 和设备是否同时变化
        if (StrUtil.notEquals(clientIp, loginInfo.getRefreshIP())
                && StrUtil.notEquals(deviceInfo, loginInfo.getRefreshDeviceInfo())) {
            // 刷新ip和登录ip不一致 并且 刷新设备信息和登录设备信息不一致，属于高风险情况
            return true;
        }

        // 第三步：调用AI服务进行精确评估（仅IP或仅设备变化的情况）
        var riskRefreshTokenResponse = this.aigcFeign.assessRefreshTokenRisk(JSONUtil.toJsonStr(loginInfo));
        // 高风险，拒绝生成新的token
        return riskRefreshTokenResponse.getRiskLevel() == RiskLevel.HIGH;
    }

}
