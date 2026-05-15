package com.itheima.chaincloud.api.driver;

import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.driver.domain.*;
import com.itheima.chaincloud.domain.driver.enums.DriverJobStatusEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@FeignClient(value = "chain-cloud-ms-driver", contextId = "DriverJob", path = "/driver-job")
public interface DriverJobFeign {

    /**
     * 更新司机作业状态，不允许 PENDING 状态，PROCESSING：出库业务，COMPLETED：入库业务
     */
    @PutMapping
    boolean updateStatus(@RequestParam("id") Long id, @RequestParam("status") DriverJobStatusEnum status);

    /**
     * 根据id获取司机作业单信息
     */
    @GetMapping("{id}")
    DriverJobDTO findById(@PathVariable("id") Long id);

    /**
     * 根据运输任务删除司机作业单
     */
    @DeleteMapping("removeByTransportTaskId/{transportTaskId}")
    boolean removeByTransportTaskId(@PathVariable("transportTaskId") Long transportTaskId);

    /**
     * 根据运输任务生成司机作业单
     */
    @PostMapping("createDriverJob/{transportTaskId}/{driverId}")
    Long createDriverJob(@PathVariable("transportTaskId") Long transportTaskId, @PathVariable("driverId") Long driverId);

    /**
     * 司机入库，修改运单的当前节点和下个节点 以及 修改运单为待调度状态，结束运输任务
     */
    @PostMapping("intoStorage")
    void intoStorage(@RequestBody DriverDeliverDTO driverDeliverDTO);

    /**
     * 司机出库，修改运单为运输中状态，开始运输任务
     */
    @PostMapping("outStorage")
    void outStorage(@RequestBody DriverPickUpDTO driverPickUpDTO);

    /**
     * 延迟提货,延迟时间以提货时间往后推最多不超过2小时
     */
    @PutMapping("delayDelivery")
    void delayedDelivery(@RequestBody DriverDelayDeliveryDTO driverDelayDeliveryDTO);

    /**
     * 回车登记
     */
    @PostMapping("returnRegister")
    void returnRegister(@RequestBody DriverReturnRegisterDTO driverReturnRegisterDTO);

    /**
     * 司机作业单月度统计
     */
    @GetMapping("jobMonthlyStatistics/{driverId}/{month}")
    DriverJobStatisticsDTO jobMonthlyStatistics(@PathVariable("driverId") String driverId, @PathVariable("month") String month);

    /**
     * 分页查询
     */
    @PostMapping("pageQuery")
    PageResponse<DriverJobDTO> pageQuery(@RequestBody DriverJobPageQueryDTO driverJobPageQueryDTO);
}
