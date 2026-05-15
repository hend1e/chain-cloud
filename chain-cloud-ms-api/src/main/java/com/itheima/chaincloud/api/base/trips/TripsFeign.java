package com.itheima.chaincloud.api.base.trips;

import com.itheima.chaincloud.domain.base.truck.TransportTripsTruckDriverDTO;
import com.itheima.chaincloud.domain.base.truck.TruckTripsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@FeignClient(name = "chain-cloud-ms-base", contextId = "TripsType", path = "base/trips")
//@RequestMapping("base/transportLine/trips")
@ApiIgnore
public interface TripsFeign {
    /**
     * 添加车次
     *
     * @param dto 车次信息
     * @return 车次信息
     */
    @PostMapping
    TruckTripsDTO save(@RequestBody TruckTripsDTO dto);

    /**
     * 根据id获取车次详情
     *
     * @param id 车次id
     * @return 车次信息
     */
    @GetMapping("/{id}")
    TruckTripsDTO fineById(@PathVariable(name = "id") Long id);

    /**
     * 获取车次列表
     *
     * @param transportLineId 线路id
     * @param ids             车次id列表
     * @return 车次列表
     */
    @GetMapping
    List<TruckTripsDTO> findAll(@RequestParam(name = "transportLineId", required = false) Long transportLineId, @RequestParam(name = "ids", required = false) List<Long> ids);

    /**
     * 更新车次信息
     *
     * @param id  车次id
     * @param dto 车次信息
     * @return 车次信息
     */
    @PutMapping("/{id}")
    TruckTripsDTO update(@PathVariable(name = "id") Long id, @RequestBody TruckTripsDTO dto);

    /**
     * 删除车次信息
     *
     * @param id 车次信息
     * @return 返回信息
     */
    @PutMapping("/{id}/disable")
    void disable(@PathVariable(name = "id") Long id);

    /**
     * 批量保存车次与车辆和司机关联关系
     *
     * @param dtoList 车次与车辆和司机关联关系
     * @return 返回信息
     */
    @PostMapping("{id}/truckDriver")
    void batchSaveTruckDriver(@PathVariable(value = "id", required = false) Long transportTripsId, @RequestBody List<TransportTripsTruckDriverDTO> dtoList);

    /**
     * 获取车次与车辆和司机关联关系列表
     *
     * @param transportTripsId 车次id
     * @param truckId          车辆id
     * @param driverId           司机id
     * @return 车次与车辆和司机关联关系列表
     */
    @GetMapping("/truckDriver")
    List<TransportTripsTruckDriverDTO> findAllTruckDriverTransportTrips(@RequestParam(name = "transportTripsId", required = false) Long transportTripsId, @RequestParam(name = "truckId", required = false) Long truckId, @RequestParam(name = "driverId", required = false) Long driverId);

}
