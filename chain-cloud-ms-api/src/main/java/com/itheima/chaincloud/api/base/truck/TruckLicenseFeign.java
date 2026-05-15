package com.itheima.chaincloud.api.base.truck;

import com.itheima.chaincloud.domain.base.truck.TruckLicenseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.annotations.ApiIgnore;

@FeignClient(name = "chain-cloud-ms-base", contextId = "TruckLicense", path = "base/truck/license")
@ApiIgnore
public interface TruckLicenseFeign {
    /**
     * 保存车辆行驶证信息
     *
     * @param dto 车辆行驶证信息
     * @return 车辆行驶证信息
     */
    @PostMapping
    TruckLicenseDTO saveTruckLicense(@RequestBody TruckLicenseDTO dto);

    /**
     * 根据id获取车辆行驶证详情
     *
     * @param id 车辆行驶证id
     * @return 车辆行驶证信息
     */
    @GetMapping("/{id}")
    TruckLicenseDTO fineById(@PathVariable(name = "id") Long id);
}
