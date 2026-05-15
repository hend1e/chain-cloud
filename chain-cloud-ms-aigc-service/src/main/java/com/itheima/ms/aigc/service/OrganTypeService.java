package com.itheima.ms.aigc.service;

import com.itheima.chaincloud.domain.aigc.domain.OrganTypeResponse;

public interface OrganTypeService {

    /**
     * 机构类型识别
     *
     * @param organInfo 机构信息（包含名称、ID、父级信息等）
     * @return 机构类型识别结果
     */
    OrganTypeResponse identifyOrganType(String organInfo);
}