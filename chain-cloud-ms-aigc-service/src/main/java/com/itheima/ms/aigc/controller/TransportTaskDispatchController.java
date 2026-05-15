package com.itheima.ms.aigc.controller;

import com.itheima.ms.aigc.service.TransportTaskDispatchService;
import com.itheima.chaincloud.domain.aigc.domain.TransportTaskDispatchResponse;
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
@Api(tags = "AI 运输任务预测接口")
@RequestMapping("/aigc/transport-task-dispatch")
@RequiredArgsConstructor
public class TransportTaskDispatchController {

    private final TransportTaskDispatchService transportOrderTaskService;

    /**
     * 运输任务预测
     *
     * @param question 任务预测问题内容（JSON格式的运输信息）
     *                示例：
     *                {
     *                  "usedWeightRatio": 0.75,
     *                  "usedVolumeRatio": 0.70,
     *                  "orderCount": 15,
     *                  "avgWeight": 120.0,
     *                  "stdWeight": 30.0,
     *                  "maxOrderWeightRatio": 0.2,
     *                  "skippedOrderCount": 2,
     *                  "remainingOrderCount": 5,
     *                  "remainingHighPriorityRatio": 0.3,
     *                  "rounds": 2
     *                }
     * @return 任务预测结果（包含预测标签、置信度和原因）
     */
    @PostMapping("/chat")
    @ApiOperation("运输任务预测 - 根据运输信息进行延误预测和建议")
    public TransportTaskDispatchResponse transportTaskDispatch(@RequestBody String question) {
        return transportOrderTaskService.transportOrderTask(question);
    }
}