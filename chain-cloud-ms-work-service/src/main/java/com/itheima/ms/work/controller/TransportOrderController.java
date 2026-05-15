package com.itheima.ms.work.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.work.domain.TransportOrderDTO;
import com.itheima.chaincloud.domain.work.domain.request.TransportOrderQueryDTO;
import com.itheima.chaincloud.domain.work.domain.response.OrderToTransportOrderDTO;
import com.itheima.chaincloud.domain.work.domain.response.TransportOrderStatusCountDTO;
import com.itheima.chaincloud.domain.work.enums.TransportOrderStatusEnum;
import com.itheima.chaincloud.domain.work.enums.WorkExceptionEnum;
import com.itheima.ms.work.entity.TransportOrderEntity;
import com.itheima.ms.work.service.TransportOrderService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@Api(tags = "运单服务")
@RequestMapping("transport-order")
public class TransportOrderController {

    @Resource
    private TransportOrderService transportOrderService;

    @PostMapping
    public OrderToTransportOrderDTO orderToTransportOrder(@RequestParam("orderId") Long orderId) {
        TransportOrderEntity transportOrderEntity = this.transportOrderService.orderToTransportOrder(orderId);
        return BeanUtil.toBean(transportOrderEntity, OrderToTransportOrderDTO.class);
    }

    @PutMapping
    public Boolean updateStatus(@RequestParam("id") String id,
                                @RequestParam("status") TransportOrderStatusEnum status) {
        return this.transportOrderService.updateStatus(Arrays.asList(id), status);
    }

    @PostMapping("page")
    public PageResponse<TransportOrderDTO> findByPage(@RequestBody TransportOrderQueryDTO transportOrderQueryDTO) {
        Page<TransportOrderEntity> pageResult = this.transportOrderService.findByPage(transportOrderQueryDTO);
        return new PageResponse<>(pageResult, TransportOrderDTO.class);
    }

    @GetMapping("{id}")
    public TransportOrderDTO findById(@PathVariable("id") String id) {
        TransportOrderEntity transportOrder = this.transportOrderService.getById(id);
        if (ObjectUtil.isEmpty(transportOrder)) {
        }
        return BeanUtil.toBean(transportOrder, TransportOrderDTO.class);
    }

    @GetMapping("/batch")
    List<TransportOrderDTO> findByIds(@RequestParam("ids") String[] ids) {
        List<TransportOrderEntity> list = this.transportOrderService.findByIds(ids);
        if (CollUtil.isEmpty(list)) {
        }
        return BeanUtil.copyToList(list, TransportOrderDTO.class);
    }

    @GetMapping("orderId/{orderId}")
    public TransportOrderDTO findByOrderId(@PathVariable("orderId") Long orderId) {
        TransportOrderEntity transportOrder = this.transportOrderService.findByOrderId(orderId);
        if (ObjectUtil.isEmpty(transportOrder)) {
        }
        return BeanUtil.toBean(transportOrder, TransportOrderDTO.class);
    }

    @PostMapping("list")
    public List<TransportOrderDTO> findByOrderIds(@RequestParam(name = "orderIds") Long[] orderIds) {
        List<TransportOrderEntity> list = this.transportOrderService.findByOrderIds(orderIds);
        if (CollUtil.isEmpty(list)) return new ArrayList<>();
        return BeanUtil.copyToList(list, TransportOrderDTO.class);
    }

    @GetMapping("search")
    public List<TransportOrderDTO> searchById(@RequestParam("id") String id) {
        List<TransportOrderEntity> entityList = this.transportOrderService.searchById(id);
        if (CollUtil.isEmpty(entityList)) return ListUtil.empty();
        return BeanUtil.copyToList(entityList, TransportOrderDTO.class);
    }

    @GetMapping("count")
    public List<TransportOrderStatusCountDTO> findStatusCount() {
        return this.transportOrderService.findStatusCount();
    }

    @PutMapping("updateByTaskId/{taskId}")
    public boolean updateByTaskId(@PathVariable("taskId") String taskId) {
        return transportOrderService.updateByTaskId(Long.valueOf(taskId));
    }

    @PutMapping("batchUpdate")
    public Boolean updateStatus(@RequestParam("ids") List<String> ids,
                                @RequestParam("status") TransportOrderStatusEnum status) {
        return this.transportOrderService.updateStatus(ids, status);
    }

    @GetMapping("pageQueryByTaskId")
    public PageResponse<TransportOrderDTO> pageQueryByTaskId(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "taskId") String taskId,
            @RequestParam(name = "transportOrderId", required = false) String transportOrderId) {
        return transportOrderService.pageQueryByTaskId(page, pageSize, taskId, transportOrderId);
    }
}
