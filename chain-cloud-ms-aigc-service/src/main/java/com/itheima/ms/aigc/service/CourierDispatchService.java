package com.itheima.ms.aigc.service;

import com.itheima.chaincloud.domain.aigc.domain.CourierDispatchRequest;
import com.itheima.chaincloud.domain.aigc.domain.CourierDispatchResponse;

public interface CourierDispatchService {

    /**
     * 快递员智能调度
     *
     * @param request 调度请求信息（包含快递员ID列表、位置信息等）
     * @return 调度结果
     */
    CourierDispatchResponse courierDispatch(CourierDispatchRequest request);
}