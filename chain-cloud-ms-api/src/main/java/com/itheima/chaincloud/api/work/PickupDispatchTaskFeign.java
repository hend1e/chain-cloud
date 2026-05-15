package com.itheima.chaincloud.api.work;

import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.work.domain.CourierTaskCountDTO;
import com.itheima.chaincloud.domain.work.domain.PickupDispatchTaskDTO;
import com.itheima.chaincloud.domain.work.domain.TaskStatsDTO;
import com.itheima.chaincloud.domain.work.domain.request.PickupDispatchTaskPageQueryDTO;
import com.itheima.chaincloud.domain.work.domain.response.PickupDispatchTaskStatisticsDTO;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskIsDeletedEnum;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskStatusEnum;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskTypeEnum;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "chain-cloud-ms-work", contextId = "pickup-dispatch", path = "/pickup-dispatch-task")
public interface PickupDispatchTaskFeign {

    @PutMapping
    Boolean updateStatus(@RequestBody PickupDispatchTaskDTO pickupDispatchTaskDTO);

    @PutMapping("courier")
    Boolean updateCourierId(@RequestParam("ids") List<Long> ids,
                            @RequestParam("originalCourierId") Long originalCourierId,
                            @RequestParam("targetCourierId") Long targetCourierId);

    @PostMapping("page")
    PageResponse<PickupDispatchTaskDTO> findByPage(@RequestBody PickupDispatchTaskPageQueryDTO dto);

    @GetMapping("{id}")
    PickupDispatchTaskDTO findById(@PathVariable("id") Long id);

    @GetMapping("ids")
    List<PickupDispatchTaskDTO> findByIds(@RequestParam("ids") List<Long> ids);

    @DeleteMapping("ids")
    boolean deleteByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/orderId/{orderId}/{taskType}")
    List<PickupDispatchTaskDTO> findByOrderId(@PathVariable("orderId") Long orderId,
                                              @PathVariable("taskType") PickupDispatchTaskTypeEnum taskType);

    @GetMapping("count")
    List<CourierTaskCountDTO> findCountByCourierIds(@RequestParam("courierIds") List<Long> courierIds,
                                                    @RequestParam("taskType") PickupDispatchTaskTypeEnum taskType,
                                                    @RequestParam("date") String date);

    @GetMapping("todayTasks/count")
    Integer todayTasksCount(@RequestParam("courierId") Long courierId,
                            @RequestParam(value = "taskType", required = false) PickupDispatchTaskTypeEnum taskType,
                            @RequestParam(value = "status", required = false) PickupDispatchTaskStatusEnum status,
                            @RequestParam(value = "isDeleted", required = false) PickupDispatchTaskIsDeletedEnum isDeleted);

    @GetMapping("/all")
    List<PickupDispatchTaskDTO> findAll(@RequestParam(name = "courierId", required = false) Long courierId,
                                        @RequestParam(name = "taskType", required = false) PickupDispatchTaskTypeEnum taskType,
                                        @RequestParam(name = "taskStatus", required = false) PickupDispatchTaskStatusEnum taskStatus,
                                        @RequestParam(name = "isDeleted", required = false) PickupDispatchTaskIsDeletedEnum isDeleted);

    @GetMapping("/todayTasksStatistics")
    PickupDispatchTaskStatisticsDTO todayTasksStatistics(@RequestParam(value = "courierId", required = false) Long courierId);

    @GetMapping("/task-stats/{courierId}")
    TaskStatsDTO getCourierTaskStats(@PathVariable("courierId") Long courierId);
}
