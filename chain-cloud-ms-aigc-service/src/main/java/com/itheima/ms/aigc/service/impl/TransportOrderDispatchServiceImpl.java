package com.itheima.ms.aigc.service.impl;

import cn.hutool.json.JSONUtil;
import com.itheima.ms.aigc.feign.AgentServerClient;
import com.itheima.ms.aigc.feign.dto.ChatRequestDTO;
import com.itheima.ms.aigc.service.TransportOrderDispatchService;
import com.itheima.chaincloud.domain.aigc.domain.TransportOrderDispatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransportOrderDispatchServiceImpl implements TransportOrderDispatchService {

    private final AgentServerClient agentServerClient;

    @Value("${agent.transport-order.dispatch:1009}")
    private Long transportOrderDispatchAgentId;

    @Override
    public TransportOrderDispatchResponse transportOrderDispatch(String request) {
        log.info("运输订单调度请求: request={} ",request);

        var chatRequestDTO = ChatRequestDTO.builder()
                .question(request)
                .sessionId(AgentServerClient.DEFAULT_SESSION_ID)
                .userToken(AgentServerClient.DEFAULT_USER_TOKEN)
                .agentId(this.transportOrderDispatchAgentId)
                .build();

        var result = this.agentServerClient.chat(chatRequestDTO);
        var response = JSONUtil.toBean(result, TransportOrderDispatchResponse.class);

        log.info("【运输订单】调度完成，得分：{}，原因：{}", response.getScore(), response.getReason());

        return response;
    }
}