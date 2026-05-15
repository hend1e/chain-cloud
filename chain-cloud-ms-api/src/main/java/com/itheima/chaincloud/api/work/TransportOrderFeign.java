package com.itheima.chaincloud.api.work;

import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.work.domain.TransportOrderDTO;
import com.itheima.chaincloud.domain.work.domain.request.TransportOrderQueryDTO;
import com.itheima.chaincloud.domain.work.domain.response.OrderToTransportOrderDTO;
import com.itheima.chaincloud.domain.work.domain.response.TransportOrderStatusCountDTO;
import com.itheima.chaincloud.domain.work.enums.TransportOrderStatusEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运单相关Api
 */
@FeignClient(name = "chain-cloud-ms-work", contextId = "TransportOrder", path = "/transport-order")
public interface TransportOrderFeign {

    @PostMapping
    OrderToTransportOrderDTO orderToTransportOrder(@RequestParam("orderId") Long orderId);

    @PutMapping
    Boolean updateStatus(@RequestParam("id") String id,
                         @RequestParam("status") TransportOrderStatusEnum status);

    @PostMapping("page")
    PageResponse<TransportOrderDTO> findByPage(@RequestBody TransportOrderQueryDTO transportOrderQueryDTO);

    @GetMapping("/{id}")
    TransportOrderDTO findById(@PathVariable("id") String id);

    @GetMapping("/batch")
    List<TransportOrderDTO> findByIds(@RequestParam("ids") String[] id);

    @GetMapping("/orderId/{orderId}")
    TransportOrderDTO findByOrderId(@PathVariable("orderId") Long orderId);

    @PostMapping("list")
    List<TransportOrderDTO> findByOrderIds(@RequestParam(name = "orderIds") Long... orderIds);

    @GetMapping("search")
    List<TransportOrderDTO> searchById(@RequestParam("id") String id);

    @GetMapping("count")
    List<TransportOrderStatusCountDTO> findStatusCount();

    @PutMapping("updateByTaskId/{taskId}")
    boolean updateByTaskId(@PathVariable("taskId") String taskId);

    @PutMapping("batchUpdate")
    Boolean updateStatus(@RequestParam("ids") List<String> ids,
                         @RequestParam("status") TransportOrderStatusEnum status);

    @GetMapping("pageQueryByTaskId")
    PageResponse<TransportOrderDTO> pageQueryByTaskId(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "taskId") String taskId,
            @RequestParam(name = "transportOrderId", required = false) String transportOrderId);
}
