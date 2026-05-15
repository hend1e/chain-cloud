package com.itheima.ms.aigc.feign.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {

    @ApiModelProperty(value = "问题内容", required = true, example = "你是谁？")
    private String question;

    @ApiModelProperty(value = "会话 ID", required = true, example = "123")
    private String sessionId;

    @ApiModelProperty(value = "用户 Token", required = true, example = "xxxx")
    private String userToken;

    @ApiModelProperty(value = "智能体 ID", required = true, example = "1001")
    private Long agentId;
}
