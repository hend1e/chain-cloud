package com.itheima.ms.courier.service;

import com.itheima.chaincloud.domain.courier.domain.CourierStatusDTO;

/**
 * 快递员状态服务接口
 */
public interface CourierStatusService {

    /**
     * 查询快递员当前状态
     *
     * @param courierId 快递员ID
     * @return 快递员状态信息
     */
    CourierStatusDTO getCourierStatus(Long courierId);
}
