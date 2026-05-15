package com.itheima.ms.aigc.service;

import com.itheima.chaincloud.domain.aigc.domain.CostFactorResponse;

public interface CostFactorService {

    /**
     * 路线成本因子识别
     *
     * @param routeInfo 路线信息（包含起点、终点、距离、成本等信息）
     * @return 路线成本因子识别结果
     */
    CostFactorResponse identifyCostFactor(String routeInfo);
}