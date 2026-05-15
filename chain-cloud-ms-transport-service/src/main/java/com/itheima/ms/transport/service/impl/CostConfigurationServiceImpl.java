package com.itheima.ms.transport.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.domain.transport.domain.CostConfigurationDTO;
import com.itheima.ms.transport.enums.ExceptionEnum;
import com.itheima.ms.transport.enums.TransportLineEnum;
import com.itheima.ms.transport.service.CostConfigurationService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成本配置相关业务
 */
@Service
public class CostConfigurationServiceImpl implements CostConfigurationService {

    private static final String SL_TRANSPORT_COST_REDIS_KEY = "SL_TRANSPORT_COST_CONFIGURATION";

    private static final Map<Object, Object> DEFAULT_COST = Map.of(
            TransportLineEnum.TRUNK_LINE.getCode(), 0.6,
            TransportLineEnum.BRANCH_LINE.getCode(), 1.0,
            TransportLineEnum.CONNECT_LINE.getCode(), 1.5);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<CostConfigurationDTO> findConfiguration() {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(SL_TRANSPORT_COST_REDIS_KEY);
        if (ObjectUtil.isEmpty(entries)) {
            entries = DEFAULT_COST;
        }
        return entries.entrySet().stream()
                .map(v -> new CostConfigurationDTO(Convert.toInt(v.getKey()), Convert.toDouble(v.getValue())))
                .collect(Collectors.toList());
    }

    @Override
    public void saveConfiguration(List<CostConfigurationDTO> dto) {
        Map<Object, Object> map = dto.stream().collect(Collectors.toMap(v -> v.getTransportLineType().toString(), v -> v.getCost().toString()));
        stringRedisTemplate.opsForHash().putAll(SL_TRANSPORT_COST_REDIS_KEY, map);
    }

    @Override
    public Double findCostByType(Integer type) {
        if (ObjectUtil.isEmpty(type)) {
            throw new CCException(ExceptionEnum.TRANSPORT_LINE_TYPE_ERROR);
        }
        Object o = stringRedisTemplate.opsForHash().get(SL_TRANSPORT_COST_REDIS_KEY, type.toString());
        if (ObjectUtil.isNotEmpty(o)) {
            return Convert.toDouble(o);
        }
        return Convert.toDouble(DEFAULT_COST.get(type));
    }
}
