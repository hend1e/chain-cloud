package com.itheima.ms.work.controller;

import cn.hutool.core.bean.BeanUtil;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.work.domain.*;
import com.itheima.chaincloud.domain.work.domain.request.PickupDispatchTaskPageQueryDTO;
import com.itheima.chaincloud.domain.work.domain.response.PickupDispatchTaskStatisticsDTO;
import com.itheima.chaincloud.domain.work.enums.*;
import com.itheima.ms.work.entity.PickupDispatchTaskEntity;
import com.itheima.ms.work.service.PickupDispatchTaskService;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(tags = "取派件任务")
@RequestMapping("pickup-dispatch-task")
public class PickupDispatchTaskController {

    @Resource
    private PickupDispatchTaskService pickupDispatchTaskService;

    @PutMapping
    @ApiOperation(value = "更新取派件任务状态", notes = "更新状态，不允许 NEW 状态")
    public Boolean updateStatus(@RequestBody PickupDispatchTaskDTO pickupDispatchTaskDTO) {
        return this.pickupDispatchTaskService.updateStatus(pickupDispatchTaskDTO);
    }

    @PutMapping("courier")
    @ApiOperation(value = "批量改派快递员", notes = "批量改派快递员")
    public Boolean updateCourierId(@RequestParam("ids") List<Long> ids,
                                   @RequestParam("originalCourierId") Long originalCourierId,
                                   @RequestParam("targetCourierId") Long targetCourierId) {
        return this.pickupDispatchTaskService.updateCourierId(ids, originalCourierId, targetCourierId);
    }

    @PostMapping("page")
    public PageResponse<PickupDispatchTaskDTO> findByPage(@RequestBody PickupDispatchTaskPageQueryDTO dto) {
        return this.pickupDispatchTaskService.findByPage(dto);
    }

    @GetMapping("{id}")
    public PickupDispatchTaskDTO findById(@PathVariable("id") Long id) {
        PickupDispatchTaskEntity pickupDispatchTask = this.pickupDispatchTaskService.getById(id);
        if (ObjectUtil.isEmpty(pickupDispatchTask)) {
        }
        return BeanUtil.toBean(pickupDispatchTask, PickupDispatchTaskDTO.class);
    }

    @GetMapping("ids")
    public List<PickupDispatchTaskDTO> findByIds(@RequestParam("ids") List<Long> ids) {
        List<PickupDispatchTaskEntity> list = this.pickupDispatchTaskService.listByIds(ids);
        return BeanUtil.copyToList(list, PickupDispatchTaskDTO.class);
    }

    @DeleteMapping("ids")
    public boolean deleteByIds(@RequestParam("ids") List<Long> ids) {
        return this.pickupDispatchTaskService.deleteByIds(ids);
    }

    @GetMapping("/orderId/{orderId}/{taskType}")
    public List<PickupDispatchTaskDTO> findByOrderId(@PathVariable("orderId") Long orderId,
                                                     @PathVariable("taskType") PickupDispatchTaskTypeEnum taskType) {
        List<PickupDispatchTaskEntity> entities = pickupDispatchTaskService.findByOrderId(orderId, taskType);
        return BeanUtil.copyToList(entities, PickupDispatchTaskDTO.class);
    }

    @GetMapping("count")
    public List<CourierTaskCountDTO> findCountByCourierIds(@RequestParam("courierIds") List<Long> courierIds,
                                                           @RequestParam("taskType") PickupDispatchTaskTypeEnum taskType,
                                                           @RequestParam("date") String date) {
        return this.pickupDispatchTaskService.findCountByCourierIds(courierIds, taskType, date);
    }

    @GetMapping("todayTasks/count")
    public Integer todayTasksCount(@RequestParam("courierId") Long courierId,
                                   @RequestParam(value = "taskType", required = false) PickupDispatchTaskTypeEnum taskType,
                                   @RequestParam(value = "status", required = false) PickupDispatchTaskStatusEnum status,
                                   @RequestParam(value = "isDeleted", required = false) PickupDispatchTaskIsDeletedEnum isDeleted) {
        return pickupDispatchTaskService.todayTasksCount(courierId, taskType, status, isDeleted);
    }

    @GetMapping("/all")
    public List<PickupDispatchTaskDTO> findAll(@RequestParam(name = "courierId", required = false) Long courierId,
                                               @RequestParam(name = "taskType", required = false) PickupDispatchTaskTypeEnum taskType,
                                               @RequestParam(name = "taskStatus", required = false) PickupDispatchTaskStatusEnum taskStatus,
                                               @RequestParam(name = "isDeleted", required = false) PickupDispatchTaskIsDeletedEnum isDeleted) {
        return pickupDispatchTaskService.findAll(courierId, taskType, taskStatus, isDeleted);
    }

    @GetMapping("/todayTasksStatistics")
    public PickupDispatchTaskStatisticsDTO todayTasksStatistics(@RequestParam(value = "courierId", required = false) Long courierId) {
        return pickupDispatchTaskService.todayTaskStatistics(courierId);
    }

    @GetMapping("/task-stats/{courierId}")
    public TaskStatsDTO getCourierTaskStats(@PathVariable("courierId") Long courierId) {
        return pickupDispatchTaskService.getCourierTaskStats(courierId);
    }
}
