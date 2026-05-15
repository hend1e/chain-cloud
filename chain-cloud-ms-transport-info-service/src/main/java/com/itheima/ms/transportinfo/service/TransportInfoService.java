package com.itheima.ms.transportinfo.service;

import com.itheima.ms.transportinfo.entity.TransportInfoDetail;
import com.itheima.ms.transportinfo.entity.TransportInfoEntity;

public interface TransportInfoService {

    TransportInfoEntity saveOrUpdate(String transportOrderId, TransportInfoDetail infoDetail);

    TransportInfoEntity queryByTransportOrderId(String transportOrderId);
}
