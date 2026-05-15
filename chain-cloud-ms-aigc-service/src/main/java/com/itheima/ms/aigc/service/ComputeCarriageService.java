package com.itheima.ms.aigc.service;

import com.itheima.chaincloud.domain.aigc.domain.ComputeCarriageResponse;

public interface ComputeCarriageService {

    /**
     * 运费计算
     *
     * @param question 用户输入的运输信息（如：从北京到上海，5kg）
     * @return 运费计算结果
     */
    ComputeCarriageResponse computeCarriage(String question);
}
