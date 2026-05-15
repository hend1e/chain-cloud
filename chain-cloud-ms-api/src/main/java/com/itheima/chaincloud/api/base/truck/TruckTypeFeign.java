package com.itheima.chaincloud.api.base.truck;

import com.itheima.chaincloud.domain.base.truck.TruckTypeDTO;
import com.itheima.chaincloud.common.util.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@FeignClient(name = "chain-cloud-ms-base", contextId = "TruckType", path = "base/truck/type")
public interface TruckTypeFeign {
    /**
     * 添加车辆类型
     *
     * @param dto 车辆类型信息
     * @return 车辆类型信息
     */
    @PostMapping
    TruckTypeDTO saveTruckType(@RequestBody TruckTypeDTO dto);

    /**
     * 根据id获取车辆类型详情
     *
     * @param id 车辆类型id
     * @return 车辆类型信息
     */
    @GetMapping("/{id}")
    TruckTypeDTO fineById(@PathVariable(name = "id") Long id);

    /**
     * 获取车辆类型分页数据
     *
     * @param page               页码
     * @param pageSize           页尺寸
     * @param name               车辆类型名称
     * @param minAllowableLoad   车辆载重最小值（闭区间）
     * @param maxAllowableLoad   车辆载重最大值（开区间）
     * @param minAllowableVolume 车辆体积最小值（闭区间）
     * @param maxAllowableVolume 车辆体积最小值（开区间）
     * @param id                 车型id
     * @return 车辆类型分页数据
     */
    @GetMapping("/page")
    PageResponse<TruckTypeDTO> findByPage(@RequestParam(name = "page") Integer page,
                                          @RequestParam(name = "pageSize") Integer pageSize,
                                          @RequestParam(name = "name", required = false) String name,
                                          @RequestParam(name = "minAllowableLoad", required = false) BigDecimal minAllowableLoad,
                                          @RequestParam(name = "maxAllowableLoad", required = false) BigDecimal maxAllowableLoad,
                                          @RequestParam(name = "minAllowableVolume", required = false) BigDecimal minAllowableVolume,
                                          @RequestParam(name = "maxAllowableVolume", required = false) BigDecimal maxAllowableVolume,
                                          @RequestParam(name = "id", required = false) Long id);

    /**
     * 获取车辆类型列表
     *
     * @param ids 车辆类型id
     * @return 车辆类型列表
     */
    @GetMapping
    List<TruckTypeDTO> findAll(@RequestParam(name = "ids", required = false) List<Long> ids);

    /**
     * 更新车辆类型信息
     *
     * @param id  车辆类型id
     * @param dto 车辆类型信息
     * @return 车辆类型信息
     */
    @PutMapping("/{id}")
    TruckTypeDTO update(@PathVariable(name = "id") Long id, @RequestBody TruckTypeDTO dto);

    /**
     * 删除车辆类型
     *
     * @param id 车辆类型Id
     * @return 返回信息
     */
    @PutMapping("/{id}/disable")
    void disable(@PathVariable(name = "id") Long id);
}
