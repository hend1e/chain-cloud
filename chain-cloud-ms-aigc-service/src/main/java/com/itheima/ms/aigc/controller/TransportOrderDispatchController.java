package com.itheima.ms.aigc.controller;

import com.itheima.ms.aigc.service.TransportOrderDispatchService;
import com.itheima.chaincloud.domain.aigc.domain.TransportOrderDispatchResponse;
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
@Api(tags = "AI 运输订单调度接口")
@RequestMapping("/aigc/transport-order-dispatch")
@RequiredArgsConstructor
public class TransportOrderDispatchController {

    private final TransportOrderDispatchService transportOrderDispatchService;

    /**
     * 运输订单智能调度
     *
     * @param request 调度请求信息（包含运输订单信息、会话信息等）
     *                示例：
     *                {
     *                  "total_weight": 10,
     *                  "total_volume": 2,
     *                  "created": 1710000000,
     *                  "route_order_count": 20
     *                }
     * @return 调度结果（包含优先级评分和原因）
     */
    @PostMapping("/dispatch")
    @ApiOperation("运输订单智能调度 - 根据运输订单信息进行优先级评分和调度建议")
    public TransportOrderDispatchResponse transportOrderDispatch(@RequestBody String request) {
        return transportOrderDispatchService.transportOrderDispatch(request);
    }
}
