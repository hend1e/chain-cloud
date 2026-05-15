package com.itheima.ms.aigc.service.impl;

import cn.hutool.json.JSONUtil;
import com.itheima.ms.aigc.feign.AgentServerClient;
import com.itheima.ms.aigc.feign.dto.ChatRequestDTO;
import com.itheima.ms.aigc.service.CourierDispatchService;
import com.itheima.chaincloud.domain.aigc.domain.CourierDispatchRequest;
import com.itheima.chaincloud.domain.aigc.domain.CourierDispatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourierDispatchServiceImpl implements CourierDispatchService {

    private final AgentServerClient agentServerClient;

    @Value("${agent.courier.dispatch:1008}")
    private Long courierDispatchAgentId;

    @Override
    public CourierDispatchResponse courierDispatch(CourierDispatchRequest request) {
        // 使用 Hutool 将请求对象序列化为 JSON 字符串
        var questionJson = JSONUtil.toJsonStr(request);
        log.info("【快递员调度服务】开始进行快递员调度：{}", questionJson);

        var chatRequestDTO = ChatRequestDTO.builder()
                .question(questionJson)
                .sessionId(AgentServerClient.DEFAULT_SESSION_ID)
                .userToken(AgentServerClient.DEFAULT_USER_TOKEN)
                .agentId(this.courierDispatchAgentId)
                .build();

        var result = this.agentServerClient.chat(chatRequestDTO);
        var response = JSONUtil.toBean(result, CourierDispatchResponse.class);

        log.info("【快递员调度服务】调度完成，选中快递员：{}，原因：{}",
                response.getCourierId(), response.getReason());

        return response;
    }
}