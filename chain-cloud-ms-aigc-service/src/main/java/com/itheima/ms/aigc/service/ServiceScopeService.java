package com.itheima.ms.aigc.service;

import com.itheima.chaincloud.domain.aigc.domain.ServiceScopeResponse;

/**
 * 服务范围分析服务接口
 */
public interface ServiceScopeService {

    /**
     * 分析服务范围并推荐最优站点
     *
     * @param question 发件人地址信息
     * @return 服务范围分析结果
     */
    ServiceScopeResponse analyzeServiceScope(String question);
}