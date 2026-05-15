package com.itheima.ms.base.controller.base;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itheima.chaincloud.domain.base.basic.GoodsTypeDTO;
import com.itheima.chaincloud.domain.base.constants.TruckConstant;
import com.itheima.ms.base.entity.base.GoodsTypeEntity;
import com.itheima.ms.base.entity.truck.TruckTypeGoodsTypeEntity;
import com.itheima.ms.base.service.base.GoodsTypeService;
import com.itheima.ms.base.service.truck.TruckTypeGoodsTypeService;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.util.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 货物类型 业务
 */
@RestController
@RequestMapping("base/goodsType")
@Slf4j
public class GoodsTypeController {

    @Resource
    private GoodsTypeService goodsTypeService;
    @Resource
    private TruckTypeGoodsTypeService truckTypeGoodsTypeService;

    /**
     * 添加货物类型
     *
     * @param dto 货物类型信息
     * @return 货物类型信息
     */
    @PostMapping
    public GoodsTypeDTO saveGoodsType(@RequestBody GoodsTypeDTO dto) {
        GoodsTypeEntity goodsTypeEntity = BeanUtil.toBean(dto, GoodsTypeEntity.class);
        goodsTypeService.save(goodsTypeEntity);
        Long goodsTypeId = goodsTypeEntity.getId();
        if (ObjectUtil.isNotEmpty(dto.getTruckTypeIds())) {
            truckTypeGoodsTypeService.saveBatch(dto.getTruckTypeIds().stream().map(truckTypeId ->
                    TruckTypeGoodsTypeEntity.builder().truckTypeId(truckTypeId)
                            .goodsTypeId(goodsTypeId).build()
            ).collect(Collectors.toList()));
        }
        BeanUtil.copyProperties(goodsTypeEntity, dto);
        return dto;
    }

    /**
     * 根据id获取货物类型详情
     *
     * @param id 货物类型id
     * @return 货物类型信息
     */
    @GetMapping("/{id}")
    public GoodsTypeDTO fineById(@PathVariable(name = "id") Long id) {
        log.info("base --- 获取货物类型详情");
        GoodsTypeEntity pdGoodsType = goodsTypeService.getById(id);
        log.info("base ---  goodsTypeService.getById  result:{}", pdGoodsType);
        GoodsTypeDTO dto = null;
        if (ObjectUtil.isNotEmpty(pdGoodsType)) {
            dto = BeanUtil.toBean(pdGoodsType, GoodsTypeDTO.class);
            List<Long> list = truckTypeGoodsTypeService.findAll(null, dto.getId())
                    .stream().map(TruckTypeGoodsTypeEntity::getTruckTypeId)
                    .collect(Collectors.toList());
            dto.setTruckTypeIds(list);
        }
        return dto;
    }

    /**
     * 获取货物类型列表
     *
     * @return 货物类型列表
     */
    @GetMapping
    public List<GoodsTypeDTO> findAll(@RequestParam(name = "ids", required = false) List<Long> ids) {
        return goodsTypeService.findAll(ids).stream().map(pdGoodsType -> {
            GoodsTypeDTO dto = BeanUtil.toBean(pdGoodsType, GoodsTypeDTO.class);
            dto.setTruckTypeIds(truckTypeGoodsTypeService.findAll(null, dto.getId())
                    .stream()
                    .map(TruckTypeGoodsTypeEntity::getTruckTypeId)
                    .collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取分页货物类型数据
     *
     * @param page        页码
     * @param pageSize    页尺寸
     * @param name        货物类型名称
     * @param truckTypeId 车辆类型Id
     * @return 分页结果
     */
    @GetMapping("/page")
    public PageResponse<GoodsTypeDTO> findByPage(
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "pageSize") Integer pageSize,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "truckTypeId", required = false) Long truckTypeId) {
        IPage<GoodsTypeEntity> goodsTypePage = goodsTypeService.findByPage(page, pageSize, name, truckTypeId);
        List<GoodsTypeDTO> goodsTypeDtoList = goodsTypePage.getRecords().stream().map(goodsType -> {
            var dto = new GoodsTypeDTO();
            BeanUtil.copyProperties(goodsType, dto);
            dto.setTruckTypeIds(truckTypeGoodsTypeService.findAll(null, dto.getId())
                    .stream()
                    .map(TruckTypeGoodsTypeEntity::getTruckTypeId)
                    .collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
        return PageResponse.<GoodsTypeDTO>builder().items(goodsTypeDtoList).counts(goodsTypePage.getTotal()).page(page).pages(goodsTypePage.getPages()).pageSize(pageSize).build();
    }

    @GetMapping("/all")
    public List<GoodsTypeDTO> findAll() {
        return goodsTypeService.findAll().stream().map(item -> BeanUtil.toBean(item, GoodsTypeDTO.class)).collect(Collectors.toList());
    }

    /**
     * 更新货物类型信息
     *
     * @param id  货物类型id
     * @param dto 货物类型信息
     * @return 货物类型信息
     */
    @PutMapping("/{id}")
    public GoodsTypeDTO update(@PathVariable(name = "id") Long id, @RequestBody GoodsTypeDTO dto) {
        dto.setId(id);
        GoodsTypeEntity goodsType = BeanUtil.toBean(dto, GoodsTypeEntity.class);
        goodsTypeService.updateById(goodsType);
        if (ObjectUtil.isNotEmpty(dto.getTruckTypeIds())) {
            truckTypeGoodsTypeService.delete(null, id);
            List<TruckTypeGoodsTypeEntity> list = dto.getTruckTypeIds().stream().map(truckTypeId ->
                            TruckTypeGoodsTypeEntity.builder().truckTypeId(truckTypeId).goodsTypeId(id).build())
                    .collect(Collectors.toList());
            truckTypeGoodsTypeService.saveBatch(list);
        }
        return dto;
    }

    /**
     * 删除货物类型
     *
     * @param id 货物类型id
     * @return 返回信息
     */
    @PutMapping("/{id}/disable")
    public void disable(@PathVariable(name = "id") Long id) {
        GoodsTypeEntity pdGoodsType = new GoodsTypeEntity();
        pdGoodsType.setId(id);
        pdGoodsType.setStatus(TruckConstant.DATA_DISABLE_STATUS);
        goodsTypeService.updateById(pdGoodsType);
    }
}