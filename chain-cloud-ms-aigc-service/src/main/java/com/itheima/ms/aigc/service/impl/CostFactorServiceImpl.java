package com.itheima.ms.aigc.service.impl;

import cn.hutool.json.JSONUtil;
import com.itheima.ms.aigc.feign.AgentServerClient;
import com.itheima.ms.aigc.feign.dto.ChatRequestDTO;
import com.itheima.ms.aigc.service.CostFactorService;
import com.itheima.chaincloud.domain.aigc.domain.CostFactorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CostFactorServiceImpl implements CostFactorService {

    private final AgentServerClient agentServerClient;

    @Value("${agent.cost.factor:1006}")
    private Long costFactorAgentId;

    @Override
    public CostFactorResponse identifyCostFactor(String routeInfo) {
        log.info("【路线成本因子识别服务】开始识别路线成本因子：{}", routeInfo);

        var chatRequestDTO = ChatRequestDTO.builder()
                .question(routeInfo)
                .sessionId(AgentServerClient.DEFAULT_SESSION_ID)
                .userToken(AgentServerClient.DEFAULT_USER_TOKEN)
                .agentId(this.costFactorAgentId)
                .build();

        var result = this.agentServerClient.chat(chatRequestDTO);
        var response = JSONUtil.toBean(result, CostFactorResponse.class);

        log.info("【路线成本因子识别服务】识别完成，置信度：{}，判断理由：{}",
                response.getConfidence(), response.getReason());

        return response;
    }
}