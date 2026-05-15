package com.itheima.chaincloud.api.base.common;

import com.itheima.chaincloud.domain.base.basic.AreaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 角色API
 */
@FeignClient(name = "chain-cloud-ms-base", contextId = "Area", path = "/area")
@ApiIgnore
public interface AreaFeign {
    /**
     * 根据id获取行政区域详情
     *
     * @param id 行政区域id
     * @return 行政区域id
     */
    @GetMapping("/{id}")
    AreaDTO get(@PathVariable(value = "id") Long id);

    /**
     * 根据区域编码获取区域
     *
     * @param code 行政编码
     * @return 行政区域
     */
    @GetMapping("/code/{code}")
    AreaDTO getByCode(@PathVariable(value = "code") String code);

    /**
     * 根据简称获取行政区域详情
     *
     * @param shortName 行政区域简称
     * @return 行政区域列表
     */
    @GetMapping("/shortName/{shortName}")
    List<AreaDTO> getByShortName(@PathVariable(value = "shortName") String shortName);

    /**
     * 根据合并名称模糊查询行政区域
     *
     * @param mergerName 合并名称
     * @return 行政区域列表
     */
    @GetMapping("/mergerName/{mergerName}")
    List<AreaDTO> getByMergerName(@PathVariable(value = "mergerName") String mergerName);

    /**
     * 根据父级id查询子级行政区域
     *
     * @param parentId 父级id
     * @return 子级行政区域列表
     */
    @GetMapping("/children")
    List<AreaDTO> findChildren(@RequestParam("parentId") Long parentId);

    /**
     * 根据id批量查询
     *
     * @param ids id列表
     * @return 行政区域列表
     */
    @GetMapping("/batch")
    List<AreaDTO> findBatch(@RequestParam("ids") List<Long> ids);
}
