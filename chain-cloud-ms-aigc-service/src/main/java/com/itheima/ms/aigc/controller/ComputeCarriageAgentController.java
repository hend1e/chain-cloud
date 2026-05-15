package com.itheima.ms.aigc.controller;

import com.itheima.chaincloud.domain.aigc.domain.ComputeCarriageResponse;
import com.itheima.ms.aigc.service.ComputeCarriageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(tags = "AI 运费计算接口")
@RequestMapping("/aigc/carriage")
@RequiredArgsConstructor
public class ComputeCarriageAgentController {

    private final ComputeCarriageService computeCarriageService;

    /**
     * 运费计算
     *
     * @param question 用户输入的运输信息（例如：从北京到上海，5kg）
     * @return 运费计算结果
     */
    @PostMapping("/compute")
    @ApiOperation("运费计算 - 根据运输距离和重量计算运费")
    public ComputeCarriageResponse computeCarriage(
            @ApiParam("运输信息描述") @RequestParam("question") String question) {
        return computeCarriageService.computeCarriage(question);
    }
}
