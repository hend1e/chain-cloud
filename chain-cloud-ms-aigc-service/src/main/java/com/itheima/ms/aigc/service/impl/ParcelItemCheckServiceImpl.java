package com.itheima.ms.aigc.service.impl;

import cn.hutool.json.JSONUtil;
import com.itheima.ms.aigc.feign.AgentServerClient;
import com.itheima.ms.aigc.feign.dto.ChatRequestDTO;
import com.itheima.ms.aigc.service.ParcelItemCheckService;
import com.itheima.chaincloud.domain.aigc.domain.ParcelItemCheckResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParcelItemCheckServiceImpl implements ParcelItemCheckService {

    private final AgentServerClient agentServerClient;

    @Value("${agent.parcel.itemCheck:1004}")
    private Long parcelItemCheckAgentId;

    @Override
    public ParcelItemCheckResponse checkParcelItem(String itemName) {
        log.info("【物品检查服务】开始检查物品：{}", itemName);

        var chatRequestDTO = ChatRequestDTO.builder()
                .question(itemName)
                .sessionId(AgentServerClient.DEFAULT_SESSION_ID)
                .userToken(AgentServerClient.DEFAULT_USER_TOKEN)
                .agentId(this.parcelItemCheckAgentId)
                .build();

        var result = this.agentServerClient.chat(chatRequestDTO);
        var response = JSONUtil.toBean(result, ParcelItemCheckResponse.class);

        log.info("【物品检查服务】检查完成，物品：{}, 分类：{}, 是否禁寄：{}",
                response.getItemName(), response.getCategoryName(), response.getIsProhibited());

        return response;
    }
}
