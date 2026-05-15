package com.itheima.ms.transportinfo.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.domain.transportInfo.domain.TransportInfoDTO;
import com.itheima.ms.transportinfo.config.RedisConfig;
import com.itheima.ms.transportinfo.entity.TransportInfoDetail;
import com.itheima.ms.transportinfo.entity.TransportInfoEntity;
import com.itheima.ms.transportinfo.service.BloomFilterService;
import com.itheima.ms.transportinfo.service.TransportInfoService;
import org.bson.types.ObjectId;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TransportInfoServiceImpl implements TransportInfoService {

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private Cache<String, TransportInfoDTO> transportInfoCache;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private BloomFilterService bloomFilterService;

    @Override
    @CachePut(value = "transport-info", key = "#p0")
    public TransportInfoEntity saveOrUpdate(String transportOrderId, TransportInfoDetail infoDetail) {
        Query query = Query.query(Criteria.where("transportOrderId").is(transportOrderId));
        TransportInfoEntity transportInfoEntity = this.mongoTemplate.findOne(query, TransportInfoEntity.class);
        if (ObjectUtil.isEmpty(transportInfoEntity)) {
            transportInfoEntity = new TransportInfoEntity();
            transportInfoEntity.setId(ObjectId.get());
            transportInfoEntity.setTransportOrderId(transportOrderId);
            transportInfoEntity.setCreated(System.currentTimeMillis());
            transportInfoEntity.setUpdated(transportInfoEntity.getCreated());
            transportInfoEntity.setInfoList(ListUtil.toList(infoDetail));
        } else {
            transportInfoEntity.setUpdated(System.currentTimeMillis());
            transportInfoEntity.getInfoList().add(infoDetail);
        }

        transportInfoEntity = this.mongoTemplate.save(transportInfoEntity);

        this.stringRedisTemplate.convertAndSend(RedisConfig.CHANNEL_TOPIC, transportOrderId);

        this.bloomFilterService.add(transportOrderId);

        return transportInfoEntity;
    }

    @Override
    @Cacheable(value = "transport-info", key = "#p0")
    public TransportInfoEntity queryByTransportOrderId(String transportOrderId) {
        Query query = Query.query(Criteria.where("transportOrderId").is(transportOrderId));
        return this.mongoTemplate.findOne(query, TransportInfoEntity.class);
    }
}
