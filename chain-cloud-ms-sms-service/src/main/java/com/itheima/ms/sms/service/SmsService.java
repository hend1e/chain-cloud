package com.itheima.ms.sms.service;

import com.itheima.chaincloud.domain.sms.domain.SendResultDTO;
import com.itheima.chaincloud.domain.sms.domain.SmsInfoDTO;

import java.util.List;

public interface SmsService {

    /**
     * 发送短信
     *
     * @param smsInfoDTO 发送短信信息
     * @return 发送记录列表
     */
    List<SendResultDTO> sendSms(SmsInfoDTO smsInfoDTO);
}
