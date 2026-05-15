package com.itheima.ms.transport.service;

import com.itheima.ms.transport.entity.node.AgencyEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class AgencyServiceTest {

    @Resource
    private AgencyService agencyService;

    @Test
    public void testQueryByBid(){
        AgencyEntity agencyEntity = this.agencyService.queryByBid(25073L);
        System.out.println(agencyEntity);
    }

}
