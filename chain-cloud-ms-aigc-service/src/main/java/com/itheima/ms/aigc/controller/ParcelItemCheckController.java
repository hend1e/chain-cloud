package com.itheima.ms.aigc.controller;

import com.itheima.ms.aigc.service.ParcelItemCheckService;
import com.itheima.chaincloud.domain.aigc.domain.ParcelItemCheckResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(tags = "AI 包裹物品检查接口")
@RequestMapping("/aigc/parcel")
@RequiredArgsConstructor
public class ParcelItemCheckController {

    private final ParcelItemCheckService parcelItemCheckService;

    /**
     * 包裹物品检查
     *
     * @param itemName 物品名称
     * @return 检查结果
     */
    @PostMapping("/item/check")
    @ApiOperation("包裹物品检查 - 判断物品分类及是否禁寄")
    public ParcelItemCheckResponse checkParcelItem(@ApiParam("物品名称") @RequestParam("itemName") String itemName) {
        return parcelItemCheckService.checkParcelItem(itemName);
    }
}
