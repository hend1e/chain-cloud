package com.itheima.ms.aigc.controller;

import com.itheima.ms.aigc.service.ServiceScopeService;
import com.itheima.chaincloud.domain.aigc.domain.ServiceScopeResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 服务范围分析控制器
 * 用于根据发件人地址分析服务范围并推荐最优站点
 */
@Slf4j
@RestController
@Api(tags = "AI 服务范围分析接口")
@RequestMapping("/aigc/service-scope")
@RequiredArgsConstructor
public class ServiceScopeController {

    private final ServiceScopeService serviceScopeService;

    /**
     * 服务范围分析
     *
     * @param question 发件人地址信息，示例：
     * <pre>
     * 上海市浦东新区航头镇航都路18号
     * </pre>
     * @return 服务范围分析结果，包含推荐站点、候选站点、置信度等信息
     */
    @PostMapping("/analyze")
    @ApiOperation("服务范围分析 - 根据发件人地址推荐最优服务站点")
    public ServiceScopeResponse analyzeServiceScope(@ApiParam("发件人地址") @RequestParam("question") String question) {
        log.info("【服务范围分析接口】收到地址分析请求：{}", question);
        return serviceScopeService.analyzeServiceScope(question);
    }
}