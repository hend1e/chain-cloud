package com.itheima.ms.aigc.service;

import com.itheima.chaincloud.domain.aigc.domain.TransportTaskDispatchResponse;

/**
 * 运输订单任务预测服务
 */
public interface TransportTaskDispatchService {

    /**
     * 运输订单任务预测
     *
     * @param question 任务预测问题内容（JSON格式的运输订单信息）
     * @return 任务预测结果（包含预测标签、置信度和原因）
     */
    TransportTaskDispatchResponse transportOrderTask(String question);
}