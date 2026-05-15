package com.itheima.ms.transportinfo.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class BloomFilterConfig {

    @Value("${bloom.name:cc-bloom-filter}")
    private String name;

    @Value("${bloom.expectedInsertions:10000000}")
    private long expectedInsertions;

    @Value("${bloom.falseProbability:0.05d}")
    private double falseProbability;
}
