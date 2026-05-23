package com.itheima.ms.web.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.IdcardUtil;
import com.itheima.chaincloud.domain.base.enums.StatusEnum;
import com.itheima.chaincloud.api.user.MemberFeign;
import com.itheima.chaincloud.domain.user.domain.MemberDTO;
import com.itheima.ms.web.customer.bean.LoginInfo;
import com.itheima.ms.web.customer.service.MemberService;
import com.itheima.ms.web.customer.service.TokenService;
import com.itheima.ms.web.customer.service.WechatService;
import com.itheima.ms.web.customer.vo.user.MemberVO;
import com.itheima.ms.web.customer.vo.user.RealNameVerifyVO;
import com.itheima.ms.web.customer.vo.user.UserLoginRequestVO;
import com.itheima.ms.web.customer.vo.user.UserLoginVO;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.exception.CCWebException;
import com.itheima.chaincloud.common.service.RealNameVerifyService;
import com.itheima.chaincloud.common.util.HttpUtil;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.util.UserThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.Duration;

/**
 * 用户管理
 */
@Slf4j
@Service
public class MemberServiceImpl implements MemberService {

    @Resource
    private MemberFeign memberFeign;

    @Resource
    private TokenService tokenService;

    @Resource
    private WechatService wechatService;

    @Resource
    private RealNameVerifyService realNameVerifyService;

    //实名认证默认关闭
    @Value("${real-name-registration.enable}")
    private String realNameVerify;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 登录
     *
     * @param userLoginRequestVO 登录code
     * @return 用户信息
     */
    @Override
    public UserLoginVO login(UserLoginRequestVO userLoginRequestVO) throws IOException {
        //1. 调用微信开发平台的接口，根据临时登录code获取openid等信息
        var jsonObject = this.wechatService.getOpenid(userLoginRequestVO.getCode());
        var openid = jsonObject.getStr("openid");
        //2. 根据openid来确认是否为新用户，新用户进行注册，老用户无需直接注册
        var memberDTO = this.getByOpenid(openid);
        if (ObjectUtil.isEmpty(memberDTO)) {
            //新用户
            var newMember = MemberDTO.builder().openId(openid) //设置openid
                    .authId(jsonObject.getStr("unionid")) //设置平台唯一id，若当前小程序已绑定到微信开放平台帐号下会返回
                    .build();
            //注册用户
            this.save(newMember);
            //再次查询用户信息
            memberDTO = this.getByOpenid(openid);
        }

        //3. 调用微信开发平台的接口，获取用户手机号，如果用户手机号有更新，需要进行更新操作
        var phone = this.wechatService.getPhone(userLoginRequestVO.getPhoneCode());
        if (ObjectUtil.notEqual(phone, memberDTO.getPhone())) {
            //更新手机号
            memberDTO.setPhone(phone);
            this.memberFeign.update(memberDTO.getId(), memberDTO);
        }

        //4. 生成token，将用户id存储到token中
        var claims = MapUtil.<String, Object>builder()
                .put(Constants.GATEWAY.USER_ID, memberDTO.getId()) //将id存入token
                .build();
        var accessToken = this.tokenService.createAccessToken(claims);
        var refreshToken = this.tokenService.createRefreshToken(claims);

        // 记录登录信息
        this.saveLoginInfo(memberDTO.getId());

        //5. 返回封装响应数据
        return UserLoginVO
                .builder()
                .openid(openid)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .binding(StatusEnum.NORMAL.getCode())
                .build();
    }

    public void saveLoginInfo(Long userId) {
        var redisKey = REDIS_LOGIN_INFO + userId;
        var hashOps = this.stringRedisTemplate.boundHashOps(redisKey);

        // 构造登录信息
        var loginIP = HttpUtil.getClientIp();
        var loginDeviceInfo = HttpUtil.getDeviceInfo();
        var loginInfo = LoginInfo.builder()
                .loginIP(loginIP)
                .loginDeviceInfo(loginDeviceInfo)
                .refreshIP(loginIP)
                .refreshDeviceInfo(loginDeviceInfo)
                .loginTime(DateUtil.now())
                .lastRefreshTime(DateUtil.now())
                .refreshCount("0")
                .build();
        hashOps.putAll(BeanUtil.beanToMap(loginInfo, false, true));

        // 设置有效期一年
        hashOps.expire(Duration.ofDays(365));
    }

    public LoginInfo getLoginInfo(Long userId) {
        var redisKey = REDIS_LOGIN_INFO + userId;
        var hashOps = this.stringRedisTemplate.boundHashOps(redisKey);
        return BeanUtil.toBean(hashOps.entries(), LoginInfo.class);
    }

    public void updateLoginInfo(Long userId) {
        var loginInfo = this.getLoginInfo(userId);

        // 设置新的刷新ip和设备信息
        loginInfo.setRefreshIP(HttpUtil.getClientIp());
        loginInfo.setRefreshDeviceInfo(HttpUtil.getDeviceInfo());

        var redisKey = REDIS_LOGIN_INFO + userId;
        var hashOps = this.stringRedisTemplate.boundHashOps(redisKey);

        // 获取当前刷新次数并 +1
        var refreshCount = Convert.toInt(loginInfo.getRefreshCount(), 0) + 1;

        // 更新刷新时间和刷新次数
        loginInfo.setRefreshCount(Convert.toStr(refreshCount));
        loginInfo.setLastRefreshTime(DateUtil.now());
        hashOps.putAll(BeanUtil.beanToMap(loginInfo, false, true));
    }

    @Override
    public MemberVO detail(Long userId) {
        log.info("查找用户信息:{}", userId);
        MemberDTO member = memberFeign.detail(userId);
        log.info("查找用户信息:{} Result:{}", userId, member);
        MemberVO memberVO = BeanUtil.toBean(member, MemberVO.class);
        memberVO.setName(DesensitizedUtil.chineseName(memberVO.getName()));
        memberVO.setIdCardNo(DesensitizedUtil.idCardNum(memberVO.getIdCardNo(), 6, 4));
        return memberVO;
    }

    /**
     * 新增
     *
     * @param user 用户信息
     */
    @Override
    public void save(MemberDTO user) {
        memberFeign.save(user);
    }

    /**
     * 根据openid查询用户
     *
     * @param openid 微信ID
     * @return 用户信息
     */
    @Override
    public MemberDTO getByOpenid(String openid) {
        return memberFeign.detailByOpenId(openid);
    }

    /**
     * 实名认证
     *
     * @param vo 身份证号 姓名
     * @return 是否通过认证
     */
    @Override
    public RealNameVerifyVO realNameVerify(RealNameVerifyVO vo) {
        if (!ObjectUtil.isEmpty(vo.getFlag()) && !vo.getFlag()) {
            // 删除实名认证
            // 保存用户表
            MemberDTO memberDTO = MemberDTO.builder().id(UserThreadLocal.getUserId()).idCardNoVerify(StatusEnum.DISABLED.getCode()).build();
            save(memberDTO);
            vo.setFlag(true);
            return vo;
        }

        RealNameVerifyVO realNameVerifyVO = new RealNameVerifyVO();
        realNameVerifyVO.setName(DesensitizedUtil.chineseName(vo.getName()));
        realNameVerifyVO.setIdCard(DesensitizedUtil.idCardNum(vo.getIdCard(), 6, 4));
        realNameVerifyVO.setSex(IdcardUtil.getGenderByIdCard(vo.getIdCard()));
        realNameVerifyVO.setFlag(false);


        //1.校验身份证号规则
        if (!IdcardUtil.isValidCard(vo.getIdCard())) {
            return realNameVerifyVO;
        }


        //2.实名认证（校验身份证号和姓名的一致性）
        //实名认证收费，免费次数有限，请慎重使用
        if (Boolean.parseBoolean(realNameVerify)) {
            try {
                if (!realNameVerifyService.realNameVerify(vo.getName(), vo.getIdCard())) {
                    // 不通过
                    return realNameVerifyVO;
                }
            } catch (IOException e) {
                throw new CCWebException("实名认证方法执行失败");
            }
        }
        realNameVerifyVO.setFlag(true);
        // 保存用户表
        MemberDTO memberDTO = MemberDTO.builder().id(UserThreadLocal.getUserId()).name(vo.getName()).idCardNo(vo.getIdCard()).idCardNoVerify(StatusEnum.NORMAL.getCode()).build();
        save(memberDTO);
        return realNameVerifyVO;
    }

    /**
     * 删除用户
     */
    @Override
    public void del() {
        Long userId = UserThreadLocal.getUserId();
        memberFeign.del(userId);
    }

    /**
     * 更新用户
     *
     * @param vo 用户
     */
    @Override
    public void update(MemberVO vo) {
        Long userId = UserThreadLocal.getUserId();
        MemberDTO memberDTO = BeanUtil.toBean(vo, MemberDTO.class);
        memberDTO.setId(userId);
        memberFeign.update(userId, memberDTO);
    }

    @Override
    public UserLoginVO refresh(String refreshToken) {
        //通过AI进行风控检测，如果存在风险就不刷新，无风险直接刷新
        return this.tokenService.refreshToken(refreshToken);
    }
}
