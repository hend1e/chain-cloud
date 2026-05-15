package com.itheima.ms.transportinfo.mq;

import cn.hutool.core.convert.Convert;
import com.github.benmanes.caffeine.cache.Cache;
import com.itheima.chaincloud.domain.transportInfo.domain.TransportInfoDTO;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RedisMessageListener extends MessageListenerAdapter {

    @Resource
    private Cache<String, TransportInfoDTO> transportInfoCache;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String transportOrderId = Convert.toStr(message);
        this.transportInfoCache.invalidate(transportOrderId);
    }
}
