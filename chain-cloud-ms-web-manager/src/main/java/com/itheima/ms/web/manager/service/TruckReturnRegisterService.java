package com.itheima.ms.web.manager.service;

import com.itheima.ms.web.manager.vo.baseTruck.TruckReturnRegisterListVO;
import com.itheima.ms.web.manager.vo.baseTruck.TruckReturnRegisterPageQueryVO;
import com.itheima.ms.web.manager.vo.baseTruck.TruckReturnRegisterVO;
import com.itheima.chaincloud.common.util.PageResponse;

public interface TruckReturnRegisterService {

    /**
     * 分页查询回车登记列表
     *
     * @param vo 回车登记分页查询条件
     * @return 回车登记分页结果
     */
    PageResponse<TruckReturnRegisterListVO> pageQuery(TruckReturnRegisterPageQueryVO vo);

    /**
     * 回车登记详情
     *
     * @param id 回车登记id
     * @return 回车登记详情
     */
    TruckReturnRegisterVO detail(Long id);
}
