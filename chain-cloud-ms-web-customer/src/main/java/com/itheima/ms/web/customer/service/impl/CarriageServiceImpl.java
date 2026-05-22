package com.itheima.ms.web.customer.service.impl;

import com.itheima.chaincloud.api.aigc.AIGCFeign;
import com.itheima.chaincloud.domain.aigc.domain.ComputeCarriageResponse;
import com.itheima.ms.web.customer.service.CarriageService;
import com.itheima.ms.web.customer.vo.carriage.CarriageComputeResponseVO;
import com.itheima.chaincloud.common.util.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarriageServiceImpl implements CarriageService {

    private final AIGCFeign aigcFeign;

    @Override
    public CarriageComputeResponseVO compute(String question) {
//        log.info("【运费计算服务】开始计算运费：{}", question);
//
//        var response = aigcFeign.computeCarriage(question);
//
//        var resultVO = BeanUtil.toBean(response, CarriageComputeResponseVO.class);
//
//        log.info("【运费计算服务】计算完成，计费重量：{}kg, 总运费：{}元",
//                resultVO.getComputeWeight(), resultVO.getExpense());
//
//        return resultVO;
        return null;
    }
}
