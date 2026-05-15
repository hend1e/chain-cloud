package com.itheima.ms.web.customer.controller;

import com.itheima.ms.web.customer.service.CarriageService;
import com.itheima.ms.web.customer.vo.carriage.CarriageComputeResponseVO;
import com.itheima.chaincloud.common.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "运费计算")
@RestController
@RequestMapping("carriage")
@RequiredArgsConstructor
public class CarriageController {

    private final CarriageService carriageService;

    /**
     * 运费计算
     *
     * @param question 运输信息描述（例如：从北京到上海，5kg）
     * @return 运费计算结果
     */
    @PostMapping("/compute")
    @ApiOperation("运费计算 - 根据运输距离和重量计算运费")
    public R<CarriageComputeResponseVO> compute(
            @ApiParam("运输信息描述") @RequestParam("question") String question) {
        try {
            return R.success(carriageService.compute(question));
        } catch (Exception e) {
            log.error("计算出错, question = {}", question, e);
            return R.error("计算出错，请重新输入内容");
        }
    }
}
