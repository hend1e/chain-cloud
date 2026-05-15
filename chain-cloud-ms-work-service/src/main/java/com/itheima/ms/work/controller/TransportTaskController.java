package com.itheima.ms.work.controller;

import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.work.domain.*;
import com.itheima.chaincloud.domain.work.domain.request.*;
import com.itheima.chaincloud.domain.work.domain.response.TransportTaskMonthlyDistanceDTO;
import com.itheima.chaincloud.domain.work.domain.response.TransportTaskStatusCountDTO;
import com.itheima.chaincloud.domain.work.enums.TransportTaskStatusEnum;
import com.itheima.ms.work.service.TransportTaskService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@Api(tags = "运输任务")
@RequestMapping("transport-task")
public class TransportTaskController {

    @Resource
    private TransportTaskService transportTaskService;

    @PutMapping
    public Boolean updateStatus(@RequestParam("id") Long id,
                                @RequestParam("status") TransportTaskStatusEnum status) {
        return this.transportTaskService.updateStatus(id, status);
    }

    @PostMapping("page")
    PageResponse<TransportTaskDTO> findByPage(@RequestBody TransportTaskPageQueryDTO pageQueryDTO) {
        return this.transportTaskService.findByPage(pageQueryDTO);
    }

    @GetMapping("{id}")
    public TransportTaskDTO findById(@PathVariable("id") Long id) {
        return transportTaskService.findById(id);
    }

    @GetMapping("listByOrderIdOrTaskId")
    public List<TransportTaskDTO> findAllByOrderIdOrTaskId(
            @RequestParam(name = "transportOrderId", required = false) String transportOrderId,
            @RequestParam(name = "taskTransportId", required = false) Long taskTransportId) {
        return this.transportTaskService.findAllByOrderIdOrTaskId(transportOrderId, taskTransportId);
    }

    @GetMapping("count")
    public List<TransportTaskStatusCountDTO> count() {
        return transportTaskService.groupByStatus();
    }

    @PutMapping("adjust/{id}")
    public void adjust(@PathVariable(name = "id") Long id,
                       @RequestBody TaskTransportUpdateDTO dto) {
        dto.setId(id);
        this.transportTaskService.adjust(dto);
    }

    @PutMapping("startTransportTask")
    public void startTransportTask(@RequestBody TransportTaskStartDTO transportTaskStartDTO) {
        transportTaskService.startTransportTask(transportTaskStartDTO);
    }

    @PutMapping("completeTransportTask")
    public void completeTransportTask(@RequestBody TransportTaskCompleteDTO transportTaskCompleteDTO) {
        transportTaskService.completeTransportTask(transportTaskCompleteDTO);
    }

    @GetMapping("queryTransportOrderIdListById/{id}")
    public List<String> queryTransportOrderIdListById(@PathVariable("id") Long id) {
        return transportTaskService.queryTransportOrderIdListById(id);
    }

    @PutMapping("delayDelivery")
    public void delayedDelivery(@RequestBody TransportTaskDelayDeliveryDTO transportTaskDelayDeliveryDTO) {
        transportTaskService.delayedDelivery(transportTaskDelayDeliveryDTO);
    }

    @GetMapping("monthlyDistanceStatistics")
    public List<TransportTaskMonthlyDistanceDTO> monthlyDistanceStatistics(
            @RequestParam(name = "transportTaskIds") List<String> transportTaskIds,
            @RequestParam(name = "month") String month) {
        return transportTaskService.monthlyDistanceStatistics(transportTaskIds, month);
    }
}
