package com.itheima.ms.aigc.controller;

import com.itheima.ms.aigc.service.CostFactorService;
import com.itheima.chaincloud.domain.aigc.domain.CostFactorResponse;
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
@Api(tags = "AI 路线成本因子识别接口")
@RequestMapping("/aigc/cost-factor")
@RequiredArgsConstructor
public class CostFactorController {

    private final CostFactorService costFactorService;

    /**
     * 路线成本因子识别
     *
     * @param routeInfo 路线信息，示例：
     * <pre>
     * 请根据以下新建路线信息输出路线 cost 修正因子 JSON。
     *
     * 【路线基础信息】
     * - lineType: TRUNK_LINE
     * - startOrganId: 1001
     * - endOrganId: 2001
     * - startOrganName: 广州转运中心
     * - endOrganName: 武汉转运中心
     * - startOrganLevel: OLT
     * - endOrganLevel: OLT
     * - startCoordinate: 113.2644,23.1291
     * - endCoordinate: 114.3054,30.5931
     *
     * 【基础成本信息】
     * - unitCostByType: 0.80
     * - baseCost: 4077.00
     *
     * 【程序已知上下文】
     * - sameCity: false
     * - sameProvince: false
     * - crossProvince: true
     * - preCalculatedDistanceKm: 1770.90
     * - preCalculatedDurationHour: 19.20
     * - straightLineDistanceKm: 1320.00
     *
     *
     * 要求：
     * 1. 你可以自主决定是否调用高德 MCP 工具；
     * 2. 如果已有输入足够，请减少无意义工具调用；
     * 3. 你只能输出 JSON；
     * 4. 不要输出 finalCost；
     * 5. 所有 factor 必须在合法范围内；
     * 6. 信息不足时采用保守输出。
     * </pre>
     * @return 路线成本因子识别结果
     */
    @PostMapping("/identify-factor")
    @ApiOperation("路线成本因子识别 - 根据路线信息分析成本计算因子")
    public CostFactorResponse identifyCostFactor(@ApiParam("路线信息") @RequestParam("routeInfo") String routeInfo) {
        return costFactorService.identifyCostFactor(routeInfo);
    }
}