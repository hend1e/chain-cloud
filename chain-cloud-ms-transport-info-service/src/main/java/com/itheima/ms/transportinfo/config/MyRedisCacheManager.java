package com.itheima.ms.transportinfo.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;

public class MyRedisCacheManager extends RedisCacheManager {

    public MyRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        Duration duration = cacheConfig.getTtl();
        if (ObjectUtil.isNotEmpty(duration)) {
            Duration newDuration = duration.plusMinutes(RandomUtil.randomInt(1, 11));
            cacheConfig = cacheConfig.entryTtl(newDuration);
        }
        return super.createRedisCache(name, cacheConfig);
    }
}
