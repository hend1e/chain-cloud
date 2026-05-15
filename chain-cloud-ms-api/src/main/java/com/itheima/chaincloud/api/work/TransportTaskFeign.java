package com.itheima.chaincloud.api.work;

import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.work.domain.TaskTransportUpdateDTO;
import com.itheima.chaincloud.domain.work.domain.TransportTaskDTO;
import com.itheima.chaincloud.domain.work.domain.request.TransportTaskCompleteDTO;
import com.itheima.chaincloud.domain.work.domain.request.TransportTaskDelayDeliveryDTO;
import com.itheima.chaincloud.domain.work.domain.request.TransportTaskPageQueryDTO;
import com.itheima.chaincloud.domain.work.domain.request.TransportTaskStartDTO;
import com.itheima.chaincloud.domain.work.domain.response.TransportTaskMonthlyDistanceDTO;
import com.itheima.chaincloud.domain.work.domain.response.TransportTaskStatusCountDTO;
import com.itheima.chaincloud.domain.work.enums.TransportTaskStatusEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "chain-cloud-ms-work", contextId = "TransportTask", path = "/transport-task")
public interface TransportTaskFeign {

    @PutMapping
    Boolean updateStatus(@RequestParam("id") Long id,
                         @RequestParam("status") TransportTaskStatusEnum status);

    @PostMapping("page")
    PageResponse<TransportTaskDTO> findByPage(@RequestBody TransportTaskPageQueryDTO pageQueryDTO);

    @GetMapping("{id}")
    TransportTaskDTO findById(@PathVariable("id") Long id);

    @GetMapping("listByOrderIdOrTaskId")
    List<TransportTaskDTO> findAllByOrderIdOrTaskId(
            @RequestParam(name = "transportOrderId", required = false) String transportOrderId,
            @RequestParam(name = "taskTransportId", required = false) Long taskTransportId);

    @GetMapping("count")
    List<TransportTaskStatusCountDTO> count();

    @PutMapping("adjust/{id}")
    TransportTaskDTO adjust(@PathVariable(name = "id") Long id, @RequestBody TaskTransportUpdateDTO dto);

    @PutMapping("startTransportTask")
    void startTransportTask(@RequestBody TransportTaskStartDTO transportTaskStartDTO);

    @PutMapping("completeTransportTask")
    @ApiOperation(value = "完成运输任务")
    void completeTransportTask(@RequestBody TransportTaskCompleteDTO transportTaskCompleteDTO);

    @GetMapping("queryTransportOrderIdListById/{id}")
    List<String> queryTransportOrderIdListById(@PathVariable("id") Long id);

    @PutMapping("delayDelivery")
    void delayedDelivery(@RequestBody TransportTaskDelayDeliveryDTO transportTaskDelayDeliveryDTO);

    @GetMapping("monthlyDistanceStatistics")
    List<TransportTaskMonthlyDistanceDTO> monthlyDistanceStatistics(
            @RequestParam(name = "transportTaskIds") List<String> transportTaskIds,
            @RequestParam(name = "month") String month);

    @GetMapping("findByAgencyId")
    List<Long> findByAgencyId(@RequestParam(name = "startAgencyId", required = false) Long startAgencyId,
                              @RequestParam(name = "endAgencyId", required = false) Long endAgencyId);

    @GetMapping("countByTruckId")
    Long countByTruckId(@RequestParam(name = "truckId", required = false) Long truckId);
}
