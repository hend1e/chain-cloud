package com.itheima.chaincloud.domain.sms.domain;

import com.itheima.chaincloud.domain.sms.enums.SendStatusEnum;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("发送结果")
public class SendResultDTO {

    /**
     * 发送记录id
     */
    private Long id;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 发起发送请求的微服务名称
     */
    private String appName;

    /**
     * 发送批次id，用于判断这些数据是同一批次发送的
     */
    private Long batchId;

    /**
     * 发送状态，1：成功，2：失败
     */
    private SendStatusEnum status;

}
