package com.itheima.ms.carriage.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.chaincloud.api.base.common.AreaFeign;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.util.StrUtil;
import com.itheima.chaincloud.domain.carriage.constant.CarriageConstant;
import com.itheima.chaincloud.domain.carriage.domain.CarriageDTO;
import com.itheima.chaincloud.domain.carriage.domain.WaybillDTO;
import com.itheima.chaincloud.domain.carriage.enums.EconomicRegionEnum;
import com.itheima.ms.carriage.entity.CarriageEntity;
import com.itheima.ms.carriage.enums.CarriageExceptionEnum;
import com.itheima.ms.carriage.mapper.CarriageMapper;
import com.itheima.ms.carriage.service.CarriageService;
import com.itheima.ms.carriage.utils.CarriageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CarriageServiceImpl extends ServiceImpl<CarriageMapper, CarriageEntity> implements CarriageService {

    @Resource
    private AreaFeign areaFeign;

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
            if(count > 0){
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
        if(CollUtil.isNotEmpty(intersection)){
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
        //1. 根据参数查找运费模板
        CarriageEntity carriage = this.findCarriage(waybillDTO);

        //2. 计算出实际计费重量，确保最小重量为1kg
        double computeWeight = this.getComputeWeight(waybillDTO, carriage);

        //3. 计算运费，首重 + 续重，保留一位小数
        double expense = carriage.getFirstWeight() + ((computeWeight - 1) * carriage.getContinuousWeight());
        expense = NumberUtil.round(expense, 1).doubleValue();

        //4. 封装对象返回
        CarriageDTO carriageDTO = CarriageUtils.toDTO(carriage);
        carriageDTO.setExpense(expense);
        carriageDTO.setComputeWeight(computeWeight);
        return carriageDTO;
    }

    private CarriageEntity findCarriage(WaybillDTO waybillDTO) {
        //1. 校验是否为同城
        if (ObjectUtil.equals(waybillDTO.getReceiverCityId(), waybillDTO.getSenderCityId())) {
            //同城
            CarriageEntity carriageEntity = this.findByTemplateType(CarriageConstant.SAME_CITY);
            if (ObjectUtil.isNotEmpty(carriageEntity)) {
                return carriageEntity;
            }
        }

        //2. 校验是否为省内
        //2.1 获取收寄件地址省份id
        Long receiverProvinceId = this.areaFeign.get(waybillDTO.getReceiverCityId()).getParentId();
        Long senderProvinceId = this.areaFeign.get(waybillDTO.getSenderCityId()).getParentId();
        if (ObjectUtil.equal(receiverProvinceId, senderProvinceId)) {
            //2.2 查询同省运费模板
            CarriageEntity carriageEntity = this.findByTemplateType(CarriageConstant.SAME_PROVINCE);
            if (ObjectUtil.isNotEmpty(carriageEntity)) {
                return carriageEntity;
            }
        }

        //3. 校验是否为经济区互寄
        //3.1 获取经济区城市配置枚举
        var economicRegionMap = EnumUtil.getEnumMap(EconomicRegionEnum.class);
        EconomicRegionEnum economicRegionEnum = null;
        for (EconomicRegionEnum regionEnum : economicRegionMap.values()) {
            //该经济区是否全部包含收发件省id
            boolean result = ArrayUtil.containsAll(regionEnum.getValue(), receiverProvinceId, senderProvinceId);
            if (result) {
                economicRegionEnum = regionEnum;
                break;
            }
        }

        if (ObjectUtil.isNotEmpty(economicRegionEnum)) {
            //3.2 根据类型编码查询
            LambdaQueryWrapper<CarriageEntity> queryWrapper = Wrappers
                    .lambdaQuery(CarriageEntity.class)
                    .eq(CarriageEntity::getTemplateType, CarriageConstant.ECONOMIC_ZONE)
                    .eq(CarriageEntity::getTransportType, CarriageConstant.REGULAR_FAST)
                    .like(CarriageEntity::getAssociatedCity, economicRegionEnum.getCode());

            CarriageEntity carriageEntity = super.getOne(queryWrapper);
            if (ObjectUtil.isNotEmpty(carriageEntity)) {
                return carriageEntity;
            }
        }

        //4. 最后兜底，跨省模板
        return this.findByTemplateType(CarriageConstant.TRANS_PROVINCE);
    }

    /**
     * 根据体积参数与实际重量计算计费重量
     *
     * @param waybillDTO 运费计算对象
     * @param carriage   运费模板
     * @return 计费重量
     */
    private double getComputeWeight(WaybillDTO waybillDTO, CarriageEntity carriage) {
        //计算体积，按照长宽高计算 与 传入的体积 取最大值
        Integer volume;
        try {
            volume = NumberUtil.max(waybillDTO.getVolume(), waybillDTO.getMeasureLong() * waybillDTO.getMeasureWidth() * waybillDTO.getMeasureHigh());
        } catch (Exception e) {
            volume = 0;
        }

        // 计算体积重量，体积 / 轻抛系数
        var volumeWeight = NumberUtil.div(volume, carriage.getLightThrowingCoefficient(), 1);

        //取大值
        double computeWeight = NumberUtil.max(volumeWeight.doubleValue(), NumberUtil.round(waybillDTO.getWeight(), 1).doubleValue());

        //计算续重，规则：不满1kg，按1kg计费；10kg以下续重以0.1kg计量保留1位小数；10-100kg续重以0.5kg计量保留1位小数；100kg以上四舍五入取整
        if (computeWeight <= 1) {
            return 1;
        }

        if (computeWeight <= 10) {
            return computeWeight;
        }

        // 举例：
        // 108.4kg按照108kg收费
        // 108.5kg按照109kg收费
        // 108.6kg按照109kg收费
        if (computeWeight >= 100) {
            return NumberUtil.round(computeWeight, 0).doubleValue();
        }

        //0.5为一个计算单位，举例：
        // 18.8kg按照19收费，
        // 18.4kg按照18.5kg收费
        // 18.1kg按照18.5kg收费
        // 18.6kg按照19收费
        int integer = NumberUtil.round(computeWeight, 0, RoundingMode.DOWN).intValue();
        if (NumberUtil.sub(computeWeight, integer) == 0) {
            return integer;
        }

        if (NumberUtil.sub(computeWeight, integer) <= 0.5) {
            return NumberUtil.add(integer, 0.5);
        }
        return NumberUtil.add(integer, 1);
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
