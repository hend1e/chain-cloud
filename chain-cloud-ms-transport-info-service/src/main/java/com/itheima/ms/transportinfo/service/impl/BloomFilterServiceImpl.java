package com.itheima.ms.transportinfo.service.impl;

import com.itheima.ms.transportinfo.config.BloomFilterConfig;
import com.itheima.ms.transportinfo.service.BloomFilterService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class BloomFilterServiceImpl implements BloomFilterService {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private BloomFilterConfig bloomFilterConfig;

    private RBloomFilter<Object> getBloomFilter() {
        return this.redissonClient.getBloomFilter(this.bloomFilterConfig.getName());
    }

    @Override
    @PostConstruct
    public void init() {
        RBloomFilter<Object> bloomFilter = this.getBloomFilter();
        bloomFilter.tryInit(this.bloomFilterConfig.getExpectedInsertions(), this.bloomFilterConfig.getFalseProbability());
    }

    @Override
    public boolean add(Object obj) {
        return this.getBloomFilter().add(obj);
    }

    @Override
    public boolean contains(Object obj) {
        return this.getBloomFilter().contains(obj);
    }
}
