package com.itheima.ms.base.controller.base;

import cn.hutool.core.bean.BeanUtil;
import com.itheima.chaincloud.domain.base.basic.AreaDTO;
import com.itheima.ms.base.entity.base.AreaEntity;
import com.itheima.ms.base.service.base.AreaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器
 * 系统日志
 * </p>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/area")
@Api(value = "Area", tags = "行政区域")
public class AreaController {
    @Autowired
    private AreaService areaService;


    /**
     * 根据 id 获取行政区域详情
     *
     * @param id 行政区域 id
     * @return 行政区域 id
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据 id 获取行政区域详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "行政区域 id", required = true, dataTypeClass = Long.class)
    })
    public AreaDTO get(@PathVariable Long id) {
        var areaDto = BeanUtil.toBean(areaService.getById(id), AreaDTO.class);
        areaDto.setName(areaDto.getShortName());
        return areaDto;
    }

    @GetMapping("/code/{code}")
    @ApiOperation(value = "根据编码获取行政区域详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "行政区域编码", required = true, dataTypeClass = String.class)
    })
    public AreaDTO getByCode(@PathVariable String code) {
        var areaDto = BeanUtil.toBean((areaService.getByCode(code)), AreaDTO.class);
        areaDto.setName(areaDto.getShortName());
        return areaDto;
    }

    @GetMapping("/shortName/{shortName}")
    @ApiOperation(value = "根据简称获取行政区域详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shortName", value = "行政区域简称", required = true, dataTypeClass = String.class)
    })
    public List<AreaDTO> getByShortName(@PathVariable String shortName) {
        List<AreaEntity> entities = areaService.getByShortName(shortName);
        return entities.stream().map(area -> {
            var areaDto = BeanUtil.toBean(area, AreaDTO.class);
            areaDto.setName(areaDto.getShortName());
            return areaDto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/mergerName/{mergerName}")
    @ApiOperation(value = "根据合并名称模糊查询行政区域")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mergerName", value = "合并名称", required = true, dataTypeClass = String.class)
    })
    public List<AreaDTO> getByMergerName(@PathVariable String mergerName) {
        List<AreaEntity> entities = areaService.getByMergerName(mergerName);
        return entities.stream().map(area -> {
            var areaDto = BeanUtil.toBean(area, AreaDTO.class);
            areaDto.setName(areaDto.getShortName());
            return areaDto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/children")
    @ApiOperation(value = "根据父级id查询子级行政区域")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "parentId", value = "父级id", required = true, dataTypeClass = Long.class)})
    public List<AreaDTO> findChildren(@NotNull(message = "父id不能为空!") @RequestParam("parentId") Long parentId) {
        return areaService.findChildren(parentId);
    }

    @GetMapping("/batch")
    @ApiOperation(value = "根据id批量查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "id列表", required = true, dataTypeClass = Long.class)})
    public List<AreaDTO> findBatch(@NotNull(message = "列表不能为空!") @RequestParam("ids") List<Long> ids) {
        return areaService.findBatch(ids);
    }
}