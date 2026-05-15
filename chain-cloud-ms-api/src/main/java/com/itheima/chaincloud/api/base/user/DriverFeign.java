package com.itheima.chaincloud.api.base.user;

import com.itheima.chaincloud.domain.base.user.TruckDriverDTO;
import com.itheima.chaincloud.domain.base.user.TruckDriverLicenseDTO;
import com.itheima.chaincloud.common.util.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@FeignClient(name = "chain-cloud-ms-base", contextId = "Driver", path = "sys/driver")
@ApiIgnore
public interface DriverFeign {
    /**
     * 保存司机基本信息
     *
     * @param dto 司机基本信息
     * @return 返回信息
     */
    @PostMapping
    TruckDriverDTO saveDriver(@RequestBody TruckDriverDTO dto);

    /**
     * 获取司机基本信息列表
     *
     * @param userIds 司机id列表
     * @return 司机基本信息列表
     */
    @GetMapping
    List<TruckDriverDTO> findAllDriver(@RequestParam(name = "userIds",required = false) List<Long> userIds);

    /**
     * 获取司机基本信息
     *
     * @param id 司机id
     * @return 司机基本信息
     */
    @GetMapping("/{id}")
    TruckDriverDTO findOneDriver(@PathVariable(name = "id") Long id);

    /**
     * 保存司机驾驶证信息
     *
     * @param dto 司机驾驶证信息
     * @return 返回信息
     */
    @PostMapping("/driverLicense")
    TruckDriverLicenseDTO saveDriverLicense(@RequestBody TruckDriverLicenseDTO dto);

    /**
     * 获取司机驾驶证信息
     *
     * @param id 司机id
     * @return 司机驾驶证信息
     */
    @GetMapping("/{id}/driverLicense")
    TruckDriverLicenseDTO findOneDriverLicense(@PathVariable(name = "id") Long id);

    /**
     * 绑定司机列表
     * @param truckId 车辆ID
     * @return 司机数量
     */
    @GetMapping("/count")
    List<TruckDriverDTO> findByTruckId(@RequestParam(name = "truckId", required = false) Long truckId);

    /**
     * 获取司机分页数据
     *
     * @param page     页码
     * @param pageSize 页尺寸
     * @return 司机分页数据
     */
    @GetMapping("/page")
    PageResponse<TruckDriverDTO> findByPage(@RequestParam(name = "page") Integer page,
                                            @RequestParam(name = "pageSize") Integer pageSize);


    /**
     * 查询全部
     * @param ids
     * @return
     */
    @GetMapping("/findAll")
    List<TruckDriverDTO> findAll(@RequestParam(name = "ids", required = false) List<Long> ids);
}
