package com.itheima.ms.sms.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.ms.sms.annotation.SendChannel;
import com.itheima.ms.sms.entity.SmsRecordEntity;
import com.itheima.ms.sms.entity.SmsThirdChannelEntity;
import com.itheima.ms.sms.enums.SendChannelEnum;
import com.itheima.chaincloud.domain.sms.enums.SendStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@SendChannel(type = SendChannelEnum.ALI_YUN)
public class AliYunSmsSendHandler implements SmsSendHandler {

    @Override
    public void send(SmsThirdChannelEntity smsThirdChannelEntity, List<SmsRecordEntity> smsRecordEntities) {
        //第三方发送短信验证码
        log.info("smsRecordEntities：{}", smsRecordEntities);
        log.info("smsThirdChannelEntity：{}", smsThirdChannelEntity);
        JSONObject jsonObject = JSONUtil.parseObj(smsThirdChannelEntity.getAccount());

        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(jsonObject.getStr("accessKeyId"))
                .setAccessKeySecret(jsonObject.getStr("accessKeySecret"));
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        Client client = null;
        try {
            client = new Client(config);
        } catch (Exception e) {
            throw new CCException("阿里云短信服务配置出错！ config = " + config, e);
        }

        Client finalClient = client;
        CollUtil.forEach(smsRecordEntities, (value, index) -> {
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setSignName(smsThirdChannelEntity.getSignName())
                    .setTemplateCode(smsThirdChannelEntity.getTemplateCode())
                    .setPhoneNumbers(value.getMobile())
                    .setTemplateParam(value.getSmsContent());
            try {
                // 复制代码运行请自行打印 API 的返回值
                SendSmsResponse sendSmsResponse = finalClient.sendSmsWithOptions(sendSmsRequest, new RuntimeOptions());
                if (StrUtil.equalsAnyIgnoreCase(sendSmsResponse.getBody().getCode(), "OK")) {
                    //设置发送成功状态
                    value.setStatus(SendStatusEnum.SUCCESS);
                } else {
                    //设置发送失败状态
                    value.setStatus(SendStatusEnum.FAIL);
                    log.info("短信发送失败 ... value = {}", value);
                }
            } catch (Exception error) {
                // 如有需要，请打印 error
                value.setStatus(SendStatusEnum.FAIL);
                log.error("短信发送出现异常 ... value = {}", value, error);
            }
        });
    }
}
