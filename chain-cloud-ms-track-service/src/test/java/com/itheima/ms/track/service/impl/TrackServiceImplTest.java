package com.itheima.ms.track.service.impl;

import com.itheima.ms.track.service.TrackService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author zzj
 * @version 1.0
 */
@SpringBootTest
public class TrackServiceImplTest {

    @Resource
    private TrackService trackService;

    @Test
    public void create() {
        this.trackService.create("SL1000000000692");
    }

    @Test
    public void queryByTransportOrderId() {
    }
}
