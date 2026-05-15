package com.itheima.ms.aigc.controller;

import com.itheima.ms.aigc.service.CourierDispatchService;
import com.itheima.chaincloud.domain.aigc.domain.CourierDispatchRequest;
import com.itheima.chaincloud.domain.aigc.domain.CourierDispatchResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(tags = "AI 快递员调度接口")
@RequestMapping("/aigc/courier-dispatch")
@RequiredArgsConstructor
public class CourierDispatchController {

    private final CourierDispatchService courierDispatchService;

    /**
     * 快递员智能调度
     *
     * @param request 调度请求信息（包含快递员ID列表、位置信息等）
     * @return 调度结果
     */
    @PostMapping("/dispatch")
    @ApiOperation("快递员智能调度 - 根据快递员信息和位置进行智能调度")
    public CourierDispatchResponse courierDispatch(@RequestBody CourierDispatchRequest request) {
        return courierDispatchService.courierDispatch(request);
    }
}
