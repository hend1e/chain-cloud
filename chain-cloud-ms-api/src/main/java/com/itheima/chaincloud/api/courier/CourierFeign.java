package com.itheima.chaincloud.api.courier;

import com.itheima.chaincloud.domain.courier.domain.CourierStatusDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@ApiIgnore
@FeignClient(name = "chain-cloud-ms-courier", contextId = "Courier", path = "/couriers")
public interface CourierFeign {

    /**
     * 条件查询快递员列表（结束取件时间当天快递员有排班）
     * 如果服务范围内无快递员，或满足服务范围的快递员无排班，则返回该网点所有满足排班的快递员
     *
     * @param agencyId         网点id
     * @param longitude        用户地址的经度
     * @param latitude         用户地址的纬度
     * @param estimatedEndTime 结束取件时间
     * @return 快递员id列表
     */
    @GetMapping("{agencyId}/{longitude}/{latitude}")
    List<Long> queryCourierIdListByCondition(@PathVariable("agencyId") Long agencyId,
                                             @PathVariable("longitude") Double longitude,
                                             @PathVariable("latitude") Double latitude,
                                             @RequestParam("estimatedEndTime") Long estimatedEndTime);

    /**
     * 查询快递员当前状态
     *
     * @param courierId 快递员ID
     * @return 快递员状态信息
     */
    @GetMapping("/{courierId}/status")
    CourierStatusDTO getCourierStatus(@PathVariable("courierId") String courierId);
}
