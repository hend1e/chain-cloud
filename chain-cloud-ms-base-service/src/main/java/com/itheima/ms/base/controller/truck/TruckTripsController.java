package com.itheima.ms.base.controller.truck;

import cn.hutool.core.bean.BeanUtil;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.domain.base.truck.TransportTripsTruckDriverDTO;
import com.itheima.chaincloud.domain.base.truck.TruckTripsDTO;
import com.itheima.ms.base.entity.truck.TransportTripsTruckDriverEntity;
import com.itheima.ms.base.entity.truck.TruckTripsEntity;
import com.itheima.ms.base.service.truck.TransportTripsTruckDriverService;
import com.itheima.ms.base.service.truck.TruckTripsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 车次
 */
@RestController
@RequestMapping("base/trips")
public class TruckTripsController {

    @Resource
    private TruckTripsService truckTripsService;

    @Resource
    private TransportTripsTruckDriverService transportTripsTruckDriverService;

    /**
     * 添加车次
     *
     * @param dto 车次信息
     * @return 车次信息
     */
    @PostMapping
    public TruckTripsDTO save(@RequestBody TruckTripsDTO dto) {
        TruckTripsEntity truckTripsEntity = BeanUtil.toBean(dto, TruckTripsEntity.class);
        truckTripsService.save(truckTripsEntity);
        return dto;
    }

    /**
     * 根据id获取车次详情
     *
     * @param id 车次id
     * @return 车次信息
     */
    @GetMapping("/{id}")
    public TruckTripsDTO fineById(@PathVariable(name = "id") Long id) {
        TruckTripsEntity truckTripsEntity = truckTripsService.getById(id);

        if (ObjectUtil.isNotEmpty(truckTripsEntity)) {
            return BeanUtil.toBean(truckTripsEntity, TruckTripsDTO.class);
        }
        return new TruckTripsDTO();
    }

    /**
     * 获取车次列表
     *
     * @param transportLineId 线路id
     * @param ids             车次id列表
     * @return 车次列表
     */
    @GetMapping
    public List<TruckTripsDTO> findAll(@RequestParam(name = "transportLineId", required = false) Long transportLineId,
                                       @RequestParam(name = "ids", required = false) List<Long> ids) {
        return truckTripsService.findAll(transportLineId, ids)
                .stream()
                .map(truckTrips -> BeanUtil.toBean(truckTrips, TruckTripsDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * 更新车次信息
     *
     * @param id  车次id
     * @param dto 车次信息
     * @return 车次信息
     */
    @PutMapping("/{id}")
    public TruckTripsDTO update(@PathVariable(name = "id") Long id,
                                @RequestBody TruckTripsDTO dto) {
        dto.setId(id);
        TruckTripsEntity truckTripsEntity = BeanUtil.toBean(dto, TruckTripsEntity.class);
        truckTripsService.updateById(truckTripsEntity);
        return dto;
    }

    /**
     * 删除车次信息
     *
     * @param id 车次信息
     */
    @PutMapping("/{id}/disable")
    public void disable(@PathVariable(name = "id") Long id) {
        truckTripsService.disable(id);
    }

    /**
     * 批量保存车次与车辆和司机关联关系
     *
     * @param dtoList 车次与车辆和司机关联关系
     */
    @PostMapping("{id}/truckDriver")
    public void batchSaveTruckDriver(@PathVariable(value = "id", required = false) Long transportTripsId,
                                @RequestBody List<TransportTripsTruckDriverDTO> dtoList) {
        transportTripsTruckDriverService.batchSave(transportTripsId, dtoList.stream().map(dto -> {
            dto.setTransportTripsId(transportTripsId);
            return BeanUtil.toBean(dto, TransportTripsTruckDriverEntity.class);
        }).collect(Collectors.toList()));
    }

    /**
     * 获取车次与车辆和司机关联关系列表
     *
     * @param transportTripsId 车次id
     * @param truckId          车辆id
     * @param driverId           司机id
     * @return 车次与车辆和司机关联关系列表
     */
    @GetMapping("truckDriver")
    public List<TransportTripsTruckDriverDTO> findAllTruckDriverTransportTrips(@RequestParam(name = "transportTripsId", required = false) Long transportTripsId,
                                          @RequestParam(name = "truckId", required = false) Long truckId,
                                          @RequestParam(name = "driverId", required = false) Long driverId) {
        return transportTripsTruckDriverService.findAll(transportTripsId, truckId, driverId)
                .parallelStream()
                .map(transportTripsTruck -> BeanUtil.toBean(transportTripsTruck, TransportTripsTruckDriverDTO.class))
                .collect(Collectors.toList());
    }
}