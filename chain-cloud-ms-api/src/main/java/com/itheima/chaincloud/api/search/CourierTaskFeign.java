package com.itheima.chaincloud.api.search;

import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.search.domain.CourierTaskDTO;
import com.itheima.chaincloud.domain.search.domain.CourierTaskPageQueryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@FeignClient(value = "chain-cloud-ms-search", contextId = "CourierTask", path = "/courierSearch")
public interface CourierTaskFeign {

    /**
     * 分页查询
     *
     * @param pageQueryDTO 分页查询条件
     * @return 分页查询结果
     */
    @PostMapping("pageQuery")
    PageResponse<CourierTaskDTO> pageQuery(@RequestBody CourierTaskPageQueryDTO pageQueryDTO);

    /**
     * 新增快递员任务
     *
     * @param courierTaskDTO 快递员任务
     */
    @PostMapping
    void saveOrUpdate(@RequestBody CourierTaskDTO courierTaskDTO);

    /**
     * 根据取派件id查询快递员任务
     *
     * @param id 取派件id
     * @return 快递员任务
     */
    @GetMapping("/{id}")
    CourierTaskDTO findById(@PathVariable("id") Long id);
}
