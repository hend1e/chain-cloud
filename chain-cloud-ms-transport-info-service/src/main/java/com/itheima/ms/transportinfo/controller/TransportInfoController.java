package com.itheima.ms.transportinfo.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.BeanUtil;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.domain.transportInfo.domain.TransportInfoDTO;
import com.itheima.ms.transportinfo.entity.TransportInfoEntity;
import com.itheima.ms.transportinfo.enums.ExceptionEnum;
import com.itheima.ms.transportinfo.service.BloomFilterService;
import com.itheima.ms.transportinfo.service.TransportInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "物流信息")
@RestController
@RequestMapping("infos")
public class TransportInfoController {

    @Resource
    private TransportInfoService transportInfoService;
    @Resource
    private Cache<String, TransportInfoDTO> transportInfoCache;
    @Resource
    private BloomFilterService bloomFilterService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "transportOrderId", value = "运单id")
    })
    @ApiOperation(value = "查询", notes = "根据运单id查询物流信息")
    @GetMapping("{transportOrderId}")
    public TransportInfoDTO queryByTransportOrderId(@PathVariable("transportOrderId") String transportOrderId) {
        boolean contains = this.bloomFilterService.contains(transportOrderId);
        if (!contains) {
            throw new CCException(ExceptionEnum.NOT_FOUND);
        }

        TransportInfoDTO transportInfoDTO = this.transportInfoCache.get(transportOrderId, s -> {
            TransportInfoEntity transportInfoEntity = this.transportInfoService.queryByTransportOrderId(transportOrderId);
            return BeanUtil.toBean(transportInfoEntity, TransportInfoDTO.class);
        });

        if (ObjectUtil.isNotEmpty(transportInfoDTO)) {
            return transportInfoDTO;
        }
        throw new CCException(ExceptionEnum.NOT_FOUND);
    }
}
