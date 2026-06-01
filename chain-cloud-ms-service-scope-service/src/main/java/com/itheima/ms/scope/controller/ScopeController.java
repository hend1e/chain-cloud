package com.itheima.ms.scope.controller;

import com.itheima.chaincloud.domain.aigc.domain.ServiceScopeResponse;
import com.itheima.chaincloud.domain.scope.domain.ServiceScopeDTO;
import com.itheima.ms.scope.entity.ServiceScopeEntity;
import com.itheima.ms.scope.enums.ServiceTypeEnum;
import com.itheima.ms.scope.service.ScopeService;
import com.itheima.ms.scope.util.EntityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 服务范围
 */
@Api(tags = "服务范围")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("scopes")
public class ScopeController {

    private final ScopeService scopeService;

    /**
     * 新增或更新服务服务范围
     *
     * @return REST标准响应
     */
    @ApiOperation(value = "新增/更新", notes = "新增或更新服务服务范围")
    @PostMapping
    public ResponseEntity<Void> saveScope(@RequestBody ServiceScopeDTO serviceScopeDTO) {
        ServiceScopeEntity serviceScopeEntity = EntityUtils.toEntity(serviceScopeDTO);
        Long bid = serviceScopeEntity.getBid();
        ServiceTypeEnum type = ServiceTypeEnum.codeOf(serviceScopeEntity.getType());
        Boolean result = this.scopeService.saveOrUpdate(bid, type, serviceScopeEntity.getPolygon());
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 删除服务范围
     *
     * @param bid  业务id
     * @param type 类型
     * @return REST标准响应
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bid", value = "业务id，可以是机构或快递员", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "type", value = "类型，1-机构，2-快递员", dataTypeClass = Integer.class)
    })
    @ApiOperation(value = "删除", notes = "删除服务范围")
    @DeleteMapping("{bid}/{type}")
    public ResponseEntity<Void> delete(@NotNull(message = "bid不能为空") @PathVariable("bid") Long bid,
                                       @NotNull(message = "type不能为空") @PathVariable("type") Integer type) {
        Boolean result = this.scopeService.delete(bid, ServiceTypeEnum.codeOf(type));
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询服务范围
     *
     * @param bid  业务id
     * @param type 类型
     * @return 服务范围数据
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bid", value = "业务id，可以是机构或快递员", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "type", value = "类型，1-机构，2-快递员", dataTypeClass = Integer.class)
    })
    @ApiOperation(value = "查询", notes = "查询服务范围")
    @GetMapping("{bid}/{type}")
    public ResponseEntity<ServiceScopeDTO> queryServiceScope(@NotNull(message = "bid不能为空") @PathVariable("bid") Long bid,
                                                             @NotNull(message = "type不能为空") @PathVariable("type") Integer type) {
        ServiceScopeEntity serviceScopeEntity = this.scopeService.queryByBidAndType(bid, ServiceTypeEnum.codeOf(type));
        return ResponseEntity.ok(EntityUtils.toDTO(serviceScopeEntity));
    }

    /**
     * 地址查询服务范围
     *
     * @param type    类型，1-机构，2-快递员
     * @param address 详细地址，如：北京市昌平区金燕龙办公楼传智教育总部
     * @return 服务范围数据列表
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "类型，1-机构，2-快递员", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "address", value = "详细地址，如：北京市昌平区金燕龙办公楼传智教育总部", dataTypeClass = String.class)
    })
    @ApiOperation(value = "地址查询服务范围", notes = "地址查询服务范围")
    @GetMapping("address")
    public ResponseEntity<List<ServiceScopeDTO>> queryListByAddress(@NotNull(message = "type不能为空") @RequestParam("type") Integer type,
                                                                    @NotNull(message = "address不能为空") @RequestParam("address") String address) {
        List<ServiceScopeEntity> serviceScopeEntityList = this.scopeService.queryListByPoint(ServiceTypeEnum.codeOf(type), address);
        return ResponseEntity.ok(EntityUtils.toDTOList(serviceScopeEntityList));
    }

    /**
     * 位置查询服务范围
     *
     * @param type      类型，1-机构，2-快递员
     * @param longitude 经度
     * @param latitude  纬度
     * @return 服务范围数据列表
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "类型，1-机构，2-快递员", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "longitude", value = "经度", dataTypeClass = Double.class),
            @ApiImplicitParam(name = "latitude", value = "纬度", dataTypeClass = Double.class)
    })
    @ApiOperation(value = "位置查询服务范围", notes = "位置查询服务范围")
    @GetMapping("location")
    public ResponseEntity<List<ServiceScopeDTO>> queryListByLocation(@NotNull(message = "type不能为空") @RequestParam("type") Integer type,
                                                                     @NotNull(message = "longitude不能为空") @RequestParam("longitude") Double longitude,
                                                                     @NotNull(message = "latitude不能为空") @RequestParam("latitude") Double latitude) {
        List<ServiceScopeEntity> serviceScopeEntityList = this.scopeService.queryListByPoint(ServiceTypeEnum.codeOf(type), new GeoJsonPoint(longitude, latitude));
        return ResponseEntity.ok(EntityUtils.toDTOList(serviceScopeEntityList));
    }

    /**
     * 查询给定坐标点所在服务范围的相邻网点
     *
     * @param type      类型，1-机构，2-快递员
     * @param longitude 经度
     * @param latitude  纬度
     * @param limit     返回相邻网点数量限制，可选，默认10个
     * @return 相邻网点服务范围数据列表
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "类型，1-机构，2-快递员", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "longitude", value = "经度", dataTypeClass = Double.class),
            @ApiImplicitParam(name = "latitude", value = "纬度", dataTypeClass = Double.class),
            @ApiImplicitParam(name = "limit", value = "返回相邻网点数量限制", dataTypeClass = Integer.class)
    })
    @ApiOperation(value = "查询相邻网点", notes = "查询给定坐标点所在服务范围的相邻网点")
    @GetMapping("adjacent")
    public ResponseEntity<List<ServiceScopeDTO>> queryAdjacentScopes(@NotNull(message = "type不能为空") @RequestParam("type") Integer type,
                                                                     @NotNull(message = "longitude不能为空") @RequestParam("longitude") Double longitude,
                                                                     @NotNull(message = "latitude不能为空") @RequestParam("latitude") Double latitude,
                                                                     @RequestParam(value = "limit", required = false) Integer limit) {
        var serviceScopeEntityList = this.scopeService.queryAdjacentScopes(longitude, latitude, ServiceTypeEnum.codeOf(type), limit);
        return ResponseEntity.ok(EntityUtils.toDTOList(serviceScopeEntityList));
    }

    /**
     * 智能推荐网点接口
     * 基于AI算法分析地理位置、服务范围、负载状态等因素，智能推荐最优网点
     *
     * @param address 详细地址，如：北京市昌平区金燕龙办公楼传智教育总部
     * @return 智能推荐网点分析结果，包含推荐站点、候选排名、决策原因等
     */
    @GetMapping("recommend")
    @ApiOperation(value = "智能推荐网点", notes = "基于AI智能推荐最优网点")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "address", value = "详细地址，如：北京市昌平区金燕龙办公楼传智教育总部", dataTypeClass = String.class)
    })
    public ResponseEntity<ServiceScopeResponse> recommendAgency(@NotNull(message = "address不能为空") @RequestParam("address") String address) {

        // TODO 待实现
        return null;
    }

}
