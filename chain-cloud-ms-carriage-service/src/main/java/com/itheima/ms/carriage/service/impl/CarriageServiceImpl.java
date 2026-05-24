package com.itheima.ms.carriage.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.stream.StreamUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.util.StrUtil;
import com.itheima.chaincloud.domain.carriage.constant.CarriageConstant;
import com.itheima.chaincloud.domain.carriage.domain.CarriageDTO;
import com.itheima.chaincloud.domain.carriage.domain.WaybillDTO;
import com.itheima.ms.carriage.entity.CarriageEntity;
import com.itheima.ms.carriage.enums.CarriageExceptionEnum;
import com.itheima.ms.carriage.mapper.CarriageMapper;
import com.itheima.ms.carriage.service.CarriageService;
import com.itheima.ms.carriage.utils.CarriageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CarriageServiceImpl extends ServiceImpl<CarriageMapper, CarriageEntity> implements CarriageService {

    @Override
    public List<CarriageDTO> findAll() {
        // 构造查询条件
        LambdaQueryWrapper<CarriageEntity> queryWrapper = Wrappers.<CarriageEntity>lambdaQuery()
                //按创建时间倒序
                .orderByDesc(CarriageEntity::getCreated);
        // 查询数据库
        List<CarriageEntity> list = super.list(queryWrapper);

        //转化对象，返回集合数据
        return CollStreamUtil.toList(list, CarriageUtils::toDTO);
    }

    @Override
    public CarriageDTO saveOrUpdate(CarriageDTO carriageDto) {
        //1. 设置查询条件，查询运费模板列表
        LambdaQueryWrapper<CarriageEntity> queryWrapper = Wrappers.<CarriageEntity>lambdaQuery()
                .eq(CarriageEntity::getTemplateType, carriageDto.getTemplateType())
                .eq(CarriageEntity::getTransportType, CarriageConstant.REGULAR_FAST);
        List<CarriageEntity> list = super.list(queryWrapper);

        //2. 判断是否为经济区互寄，如果不是，需要进一步的判断是否重复，如果是，需要判断关联城市是否重复
        if (ObjectUtil.notEqual(carriageDto.getTemplateType(), CarriageConstant.ECONOMIC_ZONE)) {
            if (CollUtil.isNotEmpty(list) && ObjectUtil.isEmpty(carriageDto.getId())) {
                //模板已经存在，数据重复
                throw new CCException(CarriageExceptionEnum.NOT_ECONOMIC_ZONE_REPEAT);
            }
            //更新时判断是否已经存在该类型的模板
            long count = StreamUtil.of(list)
                    .filter(carriageEntity -> ObjectUtil.notEqual(carriageEntity.getId(), carriageDto.getId()))
                    .count();
            if (count > 0) {
                throw new CCException(CarriageExceptionEnum.NOT_ECONOMIC_ZONE_REPEAT);
            }
            //新增或更新
            return this.saveOrUpdateCarriage(carriageDto);
        }

        //3. 经济区互寄，校验关联城市是否有重复
        if (CollUtil.isEmpty(list)) {
            //直接新增或更新
            return this.saveOrUpdateCarriage(carriageDto);
        }

        //判断重复的思路：先将查询出的运费模板中的关联城市收集起来，传入的关联城市是否在此集合中
        //查询其他模板中所有的经济区列表
        List<String> associatedCityList = StreamUtil.of(list)
                //排除掉自己，检查与其他模板是否存在冲突
                .filter(carriageEntity -> ObjectUtil.notEqual(carriageEntity.getId(), carriageDto.getId()))
                //获取关联城市
                .map(CarriageEntity::getAssociatedCity)
                //将关联城市按照逗号分割
                .map(associatedCity -> StrUtil.split(associatedCity, ','))
                //将上面得到的集合展开，得到字符串
                .flatMap(StreamUtil::of)
                //收集到集合中
                .collect(Collectors.toList());

        //取交集，如果存在交集说明重复
        Collection<String> intersection = CollUtil.intersection(associatedCityList, carriageDto.getAssociatedCityList());
        if (CollUtil.isNotEmpty(intersection)) {
            throw new CCException(CarriageExceptionEnum.ECONOMIC_ZONE_CITY_REPEAT);
        }
        //不重复
        return this.saveOrUpdateCarriage(carriageDto);
    }

    private CarriageDTO saveOrUpdateCarriage(CarriageDTO carriageDto) {
        CarriageEntity carriageEntity = CarriageUtils.toEntity(carriageDto);
        boolean result = super.saveOrUpdate(carriageEntity);
        if (result) {
            return CarriageUtils.toDTO(carriageEntity);
        }
        throw new CCException(CarriageExceptionEnum.SAVE_OR_UPDATE_ERROR);
    }

    @Override
    public CarriageDTO compute(WaybillDTO waybillDTO) {
        //TODO 返回默认数据
        return CarriageDTO.builder()
                .expense(18d)
                .computeWeight(1d)
                .build();
    }


    @Override
    public CarriageEntity findByTemplateType(Integer templateType) {
        if (ObjectUtil.equals(templateType, CarriageConstant.ECONOMIC_ZONE)) {
            throw new CCException(CarriageExceptionEnum.METHOD_CALL_ERROR);
        }
        LambdaQueryWrapper<CarriageEntity> queryWrapper = Wrappers.lambdaQuery(CarriageEntity.class).eq(CarriageEntity::getTemplateType, templateType).eq(CarriageEntity::getTransportType, CarriageConstant.REGULAR_FAST);
        return super.getOne(queryWrapper);
    }
}
