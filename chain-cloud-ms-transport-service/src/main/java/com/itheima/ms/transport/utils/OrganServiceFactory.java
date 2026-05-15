package com.itheima.ms.transport.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.ms.transport.enums.ExceptionEnum;
import com.itheima.ms.transport.enums.OrganTypeEnum;
import com.itheima.ms.transport.service.AgencyService;
import com.itheima.ms.transport.service.IService;
import com.itheima.ms.transport.service.OLTService;
import com.itheima.ms.transport.service.TLTService;

/**
 * 根据type选择对应的service返回
 *
 * @author zzj
 * @version 1.0
 */
public class OrganServiceFactory {

    public static IService getBean(Integer type) {
        OrganTypeEnum organTypeEnum = OrganTypeEnum.codeOf(type);
        if (null == organTypeEnum) {
            throw new CCException(ExceptionEnum.ORGAN_TYPE_ERROR);
        }
        switch (organTypeEnum) {
            case AGENCY: {
                return SpringUtil.getBean(AgencyService.class);
            }
            case OLT: {
                return SpringUtil.getBean(OLTService.class);
            }
            case TLT: {
                return SpringUtil.getBean(TLTService.class);
            }
        }
        return null;
    }

}
