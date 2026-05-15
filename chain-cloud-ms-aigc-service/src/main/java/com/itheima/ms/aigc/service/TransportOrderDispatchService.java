package com.itheima.ms.aigc.service;

import com.itheima.chaincloud.domain.aigc.domain.TransportOrderDispatchResponse;

public interface TransportOrderDispatchService {

    /**
     * 运输智能调度
     *
     * @param request 调度请求信息（包含运输信息、会话信息等）
     * @return 调度结果（包含优先级评分和原因）
     */
    TransportOrderDispatchResponse transportOrderDispatch(String request);
}