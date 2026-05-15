package com.itheima.chaincloud.api.base.truck;

import com.itheima.chaincloud.domain.base.enums.TruckRunStatusEnum;
import com.itheima.chaincloud.domain.base.truck.TruckDTO;
import com.itheima.chaincloud.common.util.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@FeignClient(name = "chain-cloud-ms-base", contextId = "Truck", path = "base/truck")
@ApiIgnore
public interface TruckFeign {
    /**
     * 添加车辆
     *
     * @param dto 车辆信息
     * @return 车辆信息
     */
    @PostMapping
    TruckDTO saveTruck(@RequestBody TruckDTO dto);

    /**
     * 根据id获取车辆详情
     *
     * @param id 车辆id
     * @return 车辆信息
     */
    @GetMapping("/{id}")
    TruckDTO fineById(@PathVariable(name = "id") Long id);

    /**
     * 获取车辆分页数据
     *
     * @param page         页码
     * @param pageSize     页尺寸
     * @param truckTypeId  车辆类型id
     * @param status
     * @param licensePlate 车牌好吗
     * @return 车辆分页数据
     */
    @GetMapping("/page")
    PageResponse<TruckDTO> findByPage(@RequestParam(name = "page") Integer page,
                                      @RequestParam(name = "pageSize") Integer pageSize,
                                      @RequestParam(name = "truckTypeId", required = false) Long truckTypeId,
                                      @RequestParam(name = "status", required = false) Integer status,
                                      @RequestParam(name = "licensePlate", required = false) String licensePlate);

    /**
     * 获取车辆列表
     *
     * @param ids     车辆id列表
     * @return 车辆列表
     */
    @GetMapping
    List<TruckDTO> findAll(@RequestParam(name = "ids", required = false) List<Long> ids);

    /**
     * 更新车辆信息
     *
     * @param id  车辆id
     * @param dto 车辆信息
     * @return 车辆信息
     */
    @PutMapping("/{id}")
    TruckDTO update(@PathVariable(name = "id") Long id, @RequestBody TruckDTO dto);

    /**
     * 统计车辆数量
     *
     * @return 车辆数量
     */
    @GetMapping("/count")
    Map<Integer, Long> count();

    /**
     * 删除车辆
     *
     * @param id 车辆id
     * @return 返回信息
     */
    @PutMapping("/{id}/disable")
    void disable(@PathVariable(name = "id") Long id);

    /**
     * 启用车辆
     *
     * @param id 车辆id
     * @return 返回信息
     */
    @PutMapping("/{id}/enable")
    void enable(@PathVariable(name = "id") Long id);

    /**
     * 删除车辆
     *
     * @param id 车辆id
     * @return 返回信息
     */
    @PutMapping("/{id}/del")
    void del(@PathVariable(name = "id") Long id);

    /**
     * 更新车辆状态
     * 调用时机 车辆出库
     * @param id 车辆id
     * @param status 车辆状态枚举
     * @return 返回信息
     */
    @PutMapping("/updateRunStatus")
    void updateRunStatus(@RequestParam(name = "id") Long id, @RequestParam("status") TruckRunStatusEnum status);
}
