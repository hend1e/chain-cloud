package com.itheima.chaincloud.api.base.common;

import com.itheima.chaincloud.domain.base.basic.GoodsTypeDTO;
import com.itheima.chaincloud.common.util.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@FeignClient(name = "chain-cloud-ms-base", contextId = "GoodsType", path = "base/goodsType")
@ApiIgnore
public interface GoodsTypeFeign {
    /**
     * 添加货物类型
     *
     * @param dto 货物类型信息
     * @return 货物类型信息
     */
    @PostMapping
    GoodsTypeDTO saveGoodsType(@RequestBody GoodsTypeDTO dto);

    /**
     * 根据id获取货物类型详情
     *
     * @param id 货物类型id
     * @return 货物类型信息
     */
    @GetMapping("/{id}")
    GoodsTypeDTO fineById(@PathVariable(name = "id") Long id);

    /**
     * 根据id列表，获取货物类型列表
     *
     * @param ids 货物类型id
     * @return 货物类型列表
     */
    @GetMapping
    List<GoodsTypeDTO> findAll(@RequestParam(name = "ids", required = false) List<Long> ids);

    /**
     * 查询所有状态可用的货物类型列表
     *
     * @return 货物类型列表
     */
    @GetMapping("all")
    List<GoodsTypeDTO> findAll();

    /**
     * 获取分页货物类型数据
     *
     * @param page        页码
     * @param pageSize    页尺寸
     * @param name        货物类型名称
     * @param truckTypeId 车辆类型Id
     * @return 分页货物类型数据
     */
    @GetMapping("/page")
    PageResponse<GoodsTypeDTO> findByPage(
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "pageSize") Integer pageSize,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "truckTypeId", required = false) Long truckTypeId);

    /**
     * 更新货物类型信息
     *
     * @param id  货物类型id
     * @param dto 货物类型信息
     * @return 货物类型信息
     */
    @PutMapping("/{id}")
    GoodsTypeDTO update(@PathVariable(name = "id") Long id, @RequestBody GoodsTypeDTO dto);

    /**
     * 删除货物类型
     *
     * @param id 货物类型id
     * @return 返回信息
     */
    @PutMapping("/{id}/disable")
    void disable(@PathVariable(name = "id") Long id);
}
