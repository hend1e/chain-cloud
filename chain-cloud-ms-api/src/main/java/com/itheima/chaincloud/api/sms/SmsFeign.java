package com.itheima.chaincloud.api.sms;

import com.itheima.chaincloud.domain.sms.domain.SendResultDTO;
import com.itheima.chaincloud.domain.sms.domain.SmsInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@ApiIgnore
@FeignClient(name = "chain-cloud-ms-sms", contextId = "Sms", path = "/sms")
public interface SmsFeign {

    /**
     * 短信发送接口，可以同时发送给多个手机号码，多个手机号用逗号隔开
     *
     * @param smsInfoDTO
     * @return
     */
    @PostMapping("send")
    List<SendResultDTO> send(@RequestBody SmsInfoDTO smsInfoDTO);
}
