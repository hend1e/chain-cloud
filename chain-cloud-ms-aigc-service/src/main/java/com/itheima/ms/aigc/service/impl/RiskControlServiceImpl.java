package com.itheima.ms.aigc.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.ms.aigc.feign.AgentServerClient;
import com.itheima.ms.aigc.feign.dto.ChatRequestDTO;
import com.itheima.ms.aigc.service.RiskControlService;
import com.itheima.chaincloud.domain.aigc.domain.RiskLoginResponse;
import com.itheima.chaincloud.domain.aigc.domain.RiskRefreshTokenResponse;
import com.itheima.chaincloud.domain.aigc.enums.RiskLevel;
import com.itheima.chaincloud.api.auth.AuthFeign;
import com.itheima.chaincloud.domain.auth.domain.LoginRecordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskControlServiceImpl implements RiskControlService {

    private final AuthFeign authFeign;
    private final AgentServerClient agentServerClient;

    @Value("${agent.risk.login}")
    private Long riskLoginAgentId;
    @Value("${agent.risk.refreshToken}")
    private Long riskRefreshTokenAgentId;

    @Override
    public RiskLoginResponse assessLoginRisk(String account, Integer userType) {
        log.info("【风控服务】开始进行风险评估，账号：{}", account);

        // 1. 根据账号查询最近的登录信息
        var loginRecords = authFeign.queryRecordList(account, userType);

        // 2. 调用外部的 AI 服务，将最近的登录信息传递过去，由 AI 大模型进行识别风险等级
        var aiResult = callAiRiskService(account, loginRecords);

        log.info("【风控服务】风险评估完成，账号：{}, 风险等级：{}, 需要验证码：{}, 原因：{}",
                account, aiResult.getRiskLevel().getCode(), aiResult.getNeedCaptcha(), aiResult.getReason());

        return aiResult;
    }

    /**
     * 调用外部 AI 服务进行风险评估（目前模拟实现）
     *
     * @param account      账号
     * @param loginRecords 登录记录列表
     * @return 风险评估结果
     */
    private RiskLoginResponse callAiRiskService(String account, List<LoginRecordDTO> loginRecords) {
        if (CollUtil.isEmpty(loginRecords)) {
            return new RiskLoginResponse(RiskLevel.MEDIUM, true, "首次登录");
        }

        log.debug("【AI风控服务(Login)】开始调用 AI 服务，账号：{}, 登录记录数：{}", account, loginRecords.size());

        var question = StrUtil.format("评估账号：{}，最近登录信息：{}", account, JSONUtil.toJsonStr(loginRecords));
        var chatRequestDTO = ChatRequestDTO.builder()
                .question(question)
                .sessionId(AgentServerClient.DEFAULT_SESSION_ID)
                .userToken(AgentServerClient.DEFAULT_USER_TOKEN)
                .agentId(this.riskLoginAgentId)
                .build();

        var result = this.agentServerClient.chat(chatRequestDTO);
        return JSONUtil.toBean(result, RiskLoginResponse.class);
    }

    @Override
    public RiskRefreshTokenResponse assessRefreshTokenRisk(String body) {
        log.debug("【AI风控服务(RefreshToken)】开始调用 AI 服务，body：{}", body);

        var question = StrUtil.format("分析 refresh_token 风险：{}", body);
        var chatRequestDTO = ChatRequestDTO.builder()
                .question(question)
                .sessionId(AgentServerClient.DEFAULT_SESSION_ID)
                .userToken(AgentServerClient.DEFAULT_USER_TOKEN)
                .agentId(this.riskRefreshTokenAgentId)
                .build();

        var result = this.agentServerClient.chat(chatRequestDTO);
        return JSONUtil.toBean(result, RiskRefreshTokenResponse.class);
    }
}
