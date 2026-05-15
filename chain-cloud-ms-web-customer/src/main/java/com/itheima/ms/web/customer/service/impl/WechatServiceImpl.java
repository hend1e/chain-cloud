package com.itheima.ms.web.customer.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itheima.ms.web.customer.service.WechatService;
import com.itheima.chaincloud.common.exception.CCWebException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class WechatServiceImpl implements WechatService {

    @Value("${cc.wechat.appid}")
    private String appid;
    @Value("${cc.wechat.secret}")
    private String secret;

    public static final String LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final int TIMEOUT = 20000;
    public static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    public static final String PHONE_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=";

    @Override
    public JSONObject getOpenid(String code) throws IOException {
        //文档：https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
        //1. 封装参数
        var requestParam = MapUtil.<String, Object>builder().put("appid", this.appid) //小程序 appId
                .put("secret", this.secret) //小程序 appSecret
                .put("js_code", code) //	登录时获取的 code，可通过wx.login获取
                .put("grant_type", "authorization_code") //授权类型
                .build();

        //2. 发送get请求
        var response = HttpRequest.get(LOGIN_URL) //设置get请求url
                .form(requestParam) //设置表单参数
                .timeout(TIMEOUT) //设置超时时间，20s
                .execute();//执行请求
        if (response.isOk()) {
            // 3. 解析响应的结果，如果出现错误抛出异常
            var jsonObject = JSONUtil.parseObj(response.body());
            if (jsonObject.containsKey("errcode")) {
                throw new CCWebException(jsonObject.toString());
            }
            return jsonObject;
        }
        var errMsg = StrUtil.format("调用微信登录接口出错！ code = {}", code);
        throw new CCWebException(errMsg);
    }


    @Override
    public String getPhone(String code) throws IOException {
        //接口文档：https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-info/phone-number/getPhoneNumber.html

        //1. 获取手机号，需要先获取微信access_token
        var accessToken = this.getToken();
        //2. 封装参数
        var requestParam = MapUtil.<String, Object>builder()
                .put("code", code) //手机号获取凭证
                .build();
        //3. 发送post请求
        var response = HttpRequest.post(PHONE_URL + accessToken) //设置post请求url
                .body(JSONUtil.toJsonStr(requestParam)) //设置请求体参数
                .timeout(TIMEOUT) //设置超时时间，20s
                .execute();//执行请求

        if (response.isOk()) {
            // 4. 解析响应的结果，如果errcode不等于0抛出异常
            JSONObject jsonObject = JSONUtil.parseObj(response.body());
            if (ObjectUtil.notEqual(jsonObject.getInt("errcode"), 0)) {
                throw new CCWebException(jsonObject.toString());
            }
            return jsonObject.getByPath("phone_info.purePhoneNumber", String.class);
        }
        String errMsg = StrUtil.format("调用获取手机号接口出错！");
        throw new CCWebException(errMsg);
    }

    private String getToken() {
        //接口文档：https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getAccessToken.html
        //1. 封装参数
        var requestParam = MapUtil.<String, Object>builder().put("appid", this.appid) //小程序 appId
                .put("secret", this.secret) //小程序 appSecret
                .put("grant_type", "client_credential") //授权类型
                .build();
        //2. 发送get请求
        var response = HttpRequest.get(TOKEN_URL) //设置get请求url
                .form(requestParam) //设置表单参数
                .timeout(TIMEOUT) //设置超时时间，20s
                .execute();//执行请求

        if (response.isOk()) {
            // 3. 解析响应的结果，如果出现错误抛出异常
            JSONObject jsonObject = JSONUtil.parseObj(response.body());
            if (jsonObject.containsKey("errcode")) {
                throw new CCWebException(jsonObject.toString());
            }
            //TODO 缓存token到redis，不应该每次都获取token
            return jsonObject.getStr("access_token");
        }
        String errMsg = StrUtil.format("调用获取接口调用凭据接口出错！");
        throw new CCWebException(errMsg);
    }
}
