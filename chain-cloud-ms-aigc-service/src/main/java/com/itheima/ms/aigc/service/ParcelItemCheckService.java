package com.itheima.ms.aigc.service;

import com.itheima.chaincloud.domain.aigc.domain.ParcelItemCheckResponse;

public interface ParcelItemCheckService {

    /**
     * 包裹物品检查
     *
     * @param itemName 物品名称
     * @return 检查结果
     */
    ParcelItemCheckResponse checkParcelItem(String itemName);
}
