package com.itheima.chaincloud.api.transportInfo.fallback;

import com.itheima.chaincloud.api.transportInfo.TransportInfoFeign;
import com.itheima.chaincloud.domain.transportInfo.domain.TransportInfoDTO;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author zzj
 * @version 1.0
 */
public class TransportInfoFeignFallbackFactory implements FallbackFactory<TransportInfoFeign> {
    @Override
    public TransportInfoFeign create(Throwable cause) {
        return new TransportInfoFeign() {
            @Override
            public TransportInfoDTO queryByTransportOrderId(String transportOrderId) {
                return null;
            }
        };
    }
}
