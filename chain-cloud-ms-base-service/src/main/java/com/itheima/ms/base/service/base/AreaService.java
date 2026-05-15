package com.itheima.ms.base.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.chaincloud.domain.base.basic.AreaDTO;
import com.itheima.ms.base.entity.base.AreaEntity;

import java.util.List;

/**
 * <p>
 * 业务接口
 * 行政区域
 * </p>
 */
public interface AreaService extends IService<AreaEntity> {

    /**
     * 根据编码查询行政区域
     *
     * @param code 行政编码
     * @return 行政区域
     */
    AreaEntity getByCode(String code);

    /**
     * 根据简称查询行政区域
     *
     * @param shortName 行政区域简称
     * @return 行政区域列表
     */
    List<AreaEntity> getByShortName(String shortName);

    /**
     * 根据合并名称模糊查询行政区域
     *
     * @param mergerName 合并名称
     * @return 行政区域列表
     */
    List<AreaEntity> getByMergerName(String mergerName);

    /**
     * 根据父级id查询子级行政区域
     *
     * @param parentId 父级id
     * @return 子级行政区域列表
     */
    List<AreaDTO> findChildren(Long parentId);

    /**
     * 根据id批量查询
     *
     * @param ids id列表
     * @return 行政区域列表
     */
    List<AreaDTO> findBatch(List<Long> ids);
}