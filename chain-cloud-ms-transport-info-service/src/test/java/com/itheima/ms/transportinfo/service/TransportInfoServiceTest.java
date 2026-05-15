package com.itheima.ms.transportinfo.service;

import com.itheima.chaincloud.api.transport.OrganFeign;
import com.itheima.chaincloud.domain.transport.domain.OrganDTO;
import com.itheima.ms.transportinfo.entity.TransportInfoDetail;
import com.itheima.ms.transportinfo.service.TransportInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class TransportInfoServiceTest {

    @Resource
    private OrganFeign organFeign;
    @Resource
    private TransportInfoService transportInfoService;

    @Test
    public void test(){
        OrganDTO organDTO = this.organFeign.queryById(null);
        System.out.println(organDTO);
    }

    @Test
    public void test2(){
        String transportOrderId = "CC1000000000569";
        TransportInfoDetail detail = TransportInfoDetail.builder()
                .info("test")
                .build();
        this.transportInfoService.saveOrUpdate(transportOrderId, detail);
    }
}
