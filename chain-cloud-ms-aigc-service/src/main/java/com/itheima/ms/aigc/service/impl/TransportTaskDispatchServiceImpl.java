package com.itheima.ms.aigc.service.impl;

import cn.hutool.json.JSONUtil;
import com.itheima.ms.aigc.feign.AgentServerClient;
import com.itheima.ms.aigc.feign.dto.ChatRequestDTO;
import com.itheima.ms.aigc.service.TransportTaskDispatchService;
import com.itheima.chaincloud.domain.aigc.domain.TransportTaskDispatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransportTaskDispatchServiceImpl implements TransportTaskDispatchService {

    private final AgentServerClient agentServerClient;

    @Value("${agent.transport-task.dispatch:1010}")
    private Long transportTaskAgentId;

    @Override
    public TransportTaskDispatchResponse transportOrderTask(String question) {
        log.info("开始进行运输任务预测，问题内容: {}", question);

        try {
            // 构建ChatRequestDTO，使用配置值
            ChatRequestDTO chatRequestDTO = ChatRequestDTO.builder()
                    .question(question)
                    .sessionId(AgentServerClient.DEFAULT_SESSION_ID)
                    .userToken(AgentServerClient.DEFAULT_USER_TOKEN)
                    .agentId(transportTaskAgentId)
                    .build();

            // 调用AI Agent服务进行任务预测
            String response = agentServerClient.chat(chatRequestDTO);
            log.info("AI Agent服务返回结果: {}", response);

            // 解析返回的JSON结果
            TransportTaskDispatchResponse result = JSONUtil.toBean(response, TransportTaskDispatchResponse.class);

            log.info("运输任务预测完成，结果: {}", result);
            return result;

        } catch (Exception e) {
            log.error("运输任务预测失败", e);
            // 返回默认的失败结果
            return TransportTaskDispatchResponse.builder()
                    .prediction_label("error")
                    .confidence(0.0)
                    .reason("任务预测服务暂时不可用，请稍后重试")
                    .build();
        }
    }
}