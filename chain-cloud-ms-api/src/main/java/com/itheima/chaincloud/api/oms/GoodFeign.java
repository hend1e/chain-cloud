package com.itheima.chaincloud.api.oms;

import com.itheima.chaincloud.domain.oms.domain.OrderCargoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@FeignClient(name = "chain-cloud-ms-oms", path = "good", contextId = "Good")
@ApiIgnore
public interface GoodFeign {

    /**
     * 批量查询货物信息表
     *
     * @param name 名称
     * @return 货物信息
     */
    @GetMapping("/hot")
    List<OrderCargoDTO> list(@RequestParam(name = "name", required = false) String name);
}
