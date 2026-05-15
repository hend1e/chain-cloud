package com.itheima.ms.web.customer.service;

import com.itheima.ms.web.customer.vo.carriage.CarriageComputeResponseVO;

/**
 * 运费计算服务
 */
public interface CarriageService {

    /**
     * 运费计算
     *
     * @param question 运输信息描述（例如：从北京到上海，5kg）
     * @return 运费计算结果
     */
    CarriageComputeResponseVO compute(String question);
}
