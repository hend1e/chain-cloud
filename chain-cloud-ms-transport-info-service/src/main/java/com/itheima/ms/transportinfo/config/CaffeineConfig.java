package com.itheima.ms.transportinfo.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.itheima.chaincloud.domain.transportInfo.domain.TransportInfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaffeineConfig {

    @Value("${caffeine.init}")
    private Integer init;
    @Value("${caffeine.max}")
    private Integer max;

    @Bean
    public Cache<String, TransportInfoDTO> transportInfoCache() {
        return Caffeine.newBuilder()
                .initialCapacity(init)
                .maximumSize(max).build();
    }
}
