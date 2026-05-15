package com.itheima.ms.transport.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.itheima.chaincloud.domain.transport.domain.DispatchConfigurationDTO;
import com.itheima.ms.transport.service.DispatchConfigurationService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 调度服务相关业务
 */
@Service
public class DispatchConfigurationServiceImpl implements DispatchConfigurationService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    static final String DISPATCH_TIME_REDIS_KEY = "DISPATCH_CONFIGURATION:TIME";
    static final String DISPATCH_METHOD_REDIS_KEY = "DISPATCH_CONFIGURATION:METHOD";

    @Override
    public DispatchConfigurationDTO findConfiguration() {
        String dispatchTime = stringRedisTemplate.opsForValue().get(DISPATCH_TIME_REDIS_KEY);
        String dispatchMethod = stringRedisTemplate.opsForValue().get(DISPATCH_METHOD_REDIS_KEY);
        return DispatchConfigurationDTO.builder()
                .dispatchTime(Integer.parseInt(ObjectUtil.defaultIfBlank(dispatchTime, "2")))
                .dispatchMethod(Integer.parseInt(ObjectUtil.defaultIfBlank(dispatchMethod, "2")))
                .build();
    }

    @Override
    public void saveConfiguration(DispatchConfigurationDTO dto) {
        stringRedisTemplate.opsForValue().set(DISPATCH_TIME_REDIS_KEY, String.valueOf(dto.getDispatchTime()));
        stringRedisTemplate.opsForValue().set(DISPATCH_METHOD_REDIS_KEY, String.valueOf(dto.getDispatchMethod()));
    }
}
