package com.itheima.ms.aigc.service.impl;

import cn.hutool.json.JSONUtil;
import com.itheima.ms.aigc.feign.AgentServerClient;
import com.itheima.ms.aigc.feign.dto.ChatRequestDTO;
import com.itheima.ms.aigc.service.ServiceScopeService;
import com.itheima.chaincloud.domain.aigc.domain.ServiceScopeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 服务范围分析服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceScopeServiceImpl implements ServiceScopeService {

    private final AgentServerClient agentServerClient;

    @Value("${agent.service.scope:1007}")
    private Long serviceScopeAgentId;

    @Override
    public ServiceScopeResponse analyzeServiceScope(String question) {
        log.info("【服务范围分析服务】开始分析地址：{}", question);

        var chatRequestDTO = ChatRequestDTO.builder()
                .question(question)
                .sessionId(AgentServerClient.DEFAULT_SESSION_ID)
                .userToken(AgentServerClient.DEFAULT_USER_TOKEN)
                .agentId(this.serviceScopeAgentId)
                .build();

        var result = this.agentServerClient.chat(chatRequestDTO);
        var response = JSONUtil.toBean(result, ServiceScopeResponse.class);

        log.info("【服务范围分析服务】分析完成，推荐站点：{}，置信度：{}",
                response.getRecommendedStation() != null ? response.getRecommendedStation().get("station_name") : "null",
                response.getDecisionConfidence());

        return response;
    }


}