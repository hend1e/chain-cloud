package com.itheima.ms.sms.service;

import com.itheima.chaincloud.domain.sms.enums.SmsContentTypeEnum;
import com.itheima.chaincloud.domain.sms.enums.SmsTypeEnum;
import com.itheima.ms.sms.entity.SmsThirdChannelEntity;

public interface RouteService {

    /**
     * 目前只根据优先级进行路由选出前五，然后随机选择渠道
     *
     * @param smsTypeEnum        短信类型
     * @param smsContentTypeEnum 内容类型
     * @param smsCode            短信code，短信微服务发放的code，与sms_code是一对多的关系
     * @return 选中的发送通道信息
     */
    SmsThirdChannelEntity route(SmsTypeEnum smsTypeEnum, SmsContentTypeEnum smsContentTypeEnum,
                                String smsCode);
}
