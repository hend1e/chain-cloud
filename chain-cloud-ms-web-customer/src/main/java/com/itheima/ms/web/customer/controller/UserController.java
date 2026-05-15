package com.itheima.ms.web.customer.controller;

import com.itheima.ms.web.customer.service.MemberService;
import com.itheima.ms.web.customer.vo.user.MemberVO;
import com.itheima.ms.web.customer.vo.user.RealNameVerifyVO;
import com.itheima.ms.web.customer.vo.user.UserLoginRequestVO;
import com.itheima.ms.web.customer.vo.user.UserLoginVO;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.util.UserThreadLocal;
import com.itheima.chaincloud.common.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * <p>
 * 用户管理
 */
@Slf4j
@Api(tags = "用户管理")
@RestController
@RequestMapping("user")
public class UserController {

    @Resource
    private MemberService memberService;

    /**
     * C端用户登录--微信登录
     *
     * @param userLoginRequestVO 用户登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    @ApiOperation("登录")
    public R<UserLoginVO> login(@RequestBody UserLoginRequestVO userLoginRequestVO) throws IOException {
        return R.success(this.memberService.login(userLoginRequestVO));
    }

    /**
     * 刷新token，校验请求头中的长令牌，生成新的长短令牌
     *
     * @param refreshToken 原令牌
     * @return 登录结果
     */
    @PostMapping("/refresh")
    @ApiOperation("刷新token")
    public R<UserLoginVO> refresh(@RequestHeader(Constants.GATEWAY.REFRESH_TOKEN) String refreshToken) {
        var userLoginVO = this.memberService.refresh(refreshToken);
        if(ObjectUtil.isEmpty(userLoginVO)){
            return R.error("登录已过期");
        }
        return R.success(userLoginVO);
    }

    @ApiOperation(value = "更新个人信息")
    @PutMapping("profile")
    public R<Void> update(@RequestBody MemberVO vo) {
        memberService.update(vo);
        String aa ="分析 refresh_token 风险：{\"loginIP\":\"127.0.0.1\",\"loginDeviceInfo\":\"Mac OS | WeChat 8.0.5 | Mobile\",\"loginTime\":\"2026-03-23 16:12:32\",\"lastRefreshTime\":\"2026-03-23 16:12:32\",\"refreshCount\":\"0\",\"refreshIP\":\"127.0.0.1\",\"refreshDeviceInfo\":\"Mac OS | WeChat 8.0.5 | Mobile\"}";
        return R.success();
    }

    @ApiOperation(value = "我的信息")
    @GetMapping("profile")
    public R<MemberVO> profile() {
        //获取userid
        Long userId = UserThreadLocal.getUserId();
        MemberVO member = memberService.detail(userId);
        return R.success(member);
    }

    @ApiOperation(value = "解除我的信息")
    @DeleteMapping("profile")
    public R<Void> del() {
        memberService.del();
        return R.success();
    }

    /**
     * 实名认证
     *
     * @param verifyVo 姓名 & 身份证号
     * @return 是否通过认证
     */
    @ApiOperation(value = "实名认证")
    @PostMapping("realNameVerify")
    public R<RealNameVerifyVO> realNameVerify(@RequestBody RealNameVerifyVO verifyVo) {
        RealNameVerifyVO realNameVerifyVo = memberService.realNameVerify(verifyVo);
        if (realNameVerifyVo.getFlag()) {
            return R.success(realNameVerifyVo);
        }
        return R.error(realNameVerifyVo, "身份校验未通过");
    }

}
