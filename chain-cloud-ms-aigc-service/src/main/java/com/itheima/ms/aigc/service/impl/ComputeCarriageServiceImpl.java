package com.itheima.ms.aigc.service.impl;

import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.domain.aigc.domain.ComputeCarriageResponse;
import com.itheima.ms.aigc.feign.AgentServerClient;
import com.itheima.ms.aigc.feign.dto.ChatRequestDTO;
import com.itheima.ms.aigc.service.ComputeCarriageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComputeCarriageServiceImpl implements ComputeCarriageService {

    private final AgentServerClient agentServerClient;

    @Value("${agent.carriage.compute:1003}")
    private Long carriageComputeAgentId;

    @Override
    public ComputeCarriageResponse computeCarriage(String question) {
        log.info("【运费计算服务】开始计算运费：{}", question);

        var chatRequestDTO = ChatRequestDTO.builder()
                .question(question)
                .sessionId(AgentServerClient.DEFAULT_SESSION_ID)
                .userToken(AgentServerClient.DEFAULT_USER_TOKEN)
                .agentId(this.carriageComputeAgentId)
                .build();

        var result = this.agentServerClient.chat(chatRequestDTO);
        var response = JSONUtil.toBean(result, ComputeCarriageResponse.class);

        log.info("【运费计算服务】计算完成，计费重量：{}kg, 总运费：{}元",
                response.getComputeWeight(), response.getExpense());

        return response;
    }
}
