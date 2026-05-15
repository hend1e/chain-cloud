package com.itheima.chaincloud.api.trade.fallback;

import com.itheima.chaincloud.api.trade.NativePayFeign;
import com.itheima.chaincloud.domain.trade.domain.request.NativePayDTO;
import com.itheima.chaincloud.domain.trade.domain.response.NativePayResponseDTO;
import org.springframework.cloud.openfeign.FallbackFactory;

public class NativePayFeignFallbackFactory implements FallbackFactory<NativePayFeign> {
    @Override
    public NativePayFeign create(Throwable cause) {
        return new NativePayFeign() {
            @Override
            public NativePayResponseDTO createDownLineTrading(NativePayDTO nativePayDTO) {
                return null;
            }

            @Override
            public String queryQrCode(Long tradingOrderNo) {
                return null;
            }
        };
    }
}
