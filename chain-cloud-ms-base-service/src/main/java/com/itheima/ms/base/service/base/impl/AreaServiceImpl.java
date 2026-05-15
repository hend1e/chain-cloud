package com.itheima.ms.base.service.base.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.chaincloud.domain.base.basic.AreaDTO;
import com.itheima.ms.base.entity.base.AreaEntity;
import com.itheima.ms.base.mapper.base.AreaMapper;
import com.itheima.ms.base.service.base.AreaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 业务实现类
 * 行政区域
 */
@Slf4j
@Service
public class AreaServiceImpl extends ServiceImpl<AreaMapper, AreaEntity> implements AreaService {

    /**
     * 根据编码查询行政区域
     *
     * @param code 行政编码
     * @return 行政区域
     */
    @Override
    public AreaEntity getByCode(String code) {
        return baseMapper.selectOne(new LambdaQueryWrapper<AreaEntity>().eq(AreaEntity::getAreaCode, code).last(" limit 1"));
    }

    /**
     * 根据简称查询行政区域
     *
     * @param shortName 行政区域简称
     * @return 行政区域列表
     */
    @Override
    public List<AreaEntity> getByShortName(String shortName) {
        return baseMapper.selectList(new LambdaQueryWrapper<AreaEntity>().eq(AreaEntity::getShortName, shortName));
    }

    /**
     * 根据合并名称模糊查询行政区域
     *
     * @param mergerName 合并名称
     * @return 行政区域列表
     */
    @Override
    public List<AreaEntity> getByMergerName(String mergerName) {
        return baseMapper.selectList(new LambdaQueryWrapper<AreaEntity>().like(AreaEntity::getMergerName, mergerName));
    }

    /**
     * 根据父级id查询子级行政区域
     *
     * @param parentId 父级id
     * @return 子级行政区域列表
     */
    @Override
    public List<AreaDTO> findChildren(Long parentId) {
        LambdaQueryWrapper<AreaEntity> queryWrapper = Wrappers.<AreaEntity>lambdaQuery().eq(AreaEntity::getParentId, parentId);

        List<AreaEntity> entities = baseMapper.selectList(queryWrapper);

        return entities.stream().map(area -> {
            if (area.getLevel().equals(2) && "市辖区".equals(area.getName())) {
                return null;
            }
            var areaDto = BeanUtil.toBean(area, AreaDTO.class);
            areaDto.setName(areaDto.getShortName());
            return areaDto;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 根据id批量查询
     *
     * @param ids id列表
     * @return 行政区域列表
     */
    @Override
    public List<AreaDTO> findBatch(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<AreaEntity> queryWrapper = Wrappers.<AreaEntity>lambdaQuery().in(AreaEntity::getId, ids);

        List<AreaEntity> entities = baseMapper.selectList(queryWrapper);

        return entities.stream().map(area -> {
            var areaDto = BeanUtil.toBean(area, AreaDTO.class);
            areaDto.setName(areaDto.getShortName());
            return areaDto;
        }).collect(Collectors.toList());
    }
}