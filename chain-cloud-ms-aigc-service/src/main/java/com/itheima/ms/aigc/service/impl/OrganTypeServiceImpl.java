package com.itheima.ms.aigc.service.impl;

import cn.hutool.json.JSONUtil;
import com.itheima.ms.aigc.feign.AgentServerClient;
import com.itheima.ms.aigc.feign.dto.ChatRequestDTO;
import com.itheima.ms.aigc.service.OrganTypeService;
import com.itheima.chaincloud.domain.aigc.domain.OrganTypeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganTypeServiceImpl implements OrganTypeService {

    private final AgentServerClient agentServerClient;

    @Value("${agent.organ.type:1005}")
    private Long organTypeAgentId;

    @Override
    public OrganTypeResponse identifyOrganType(String organInfo) {
        log.info("【机构类型识别服务】开始识别机构类型：{}", organInfo);

        var chatRequestDTO = ChatRequestDTO.builder()
                .question(organInfo)
                .sessionId(AgentServerClient.DEFAULT_SESSION_ID)
                .userToken(AgentServerClient.DEFAULT_USER_TOKEN)
                .agentId(this.organTypeAgentId)
                .build();

        var result = this.agentServerClient.chat(chatRequestDTO);
        var response = JSONUtil.toBean(result, OrganTypeResponse.class);

        log.info("【机构类型识别服务】识别完成，机构类型：{}，置信度：{}",
                response.getOrganType(), response.getConfidence());

        return response;
    }
}