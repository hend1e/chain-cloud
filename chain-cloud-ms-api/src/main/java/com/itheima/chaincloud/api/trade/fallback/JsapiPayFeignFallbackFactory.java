package com.itheima.chaincloud.api.trade.fallback;

import com.itheima.chaincloud.api.trade.JsapiPayFeign;
import com.itheima.chaincloud.domain.trade.domain.request.JsapiPayDTO;
import com.itheima.chaincloud.domain.trade.domain.response.JsapiPayResponseDTO;
import org.springframework.cloud.openfeign.FallbackFactory;

public class JsapiPayFeignFallbackFactory implements FallbackFactory<JsapiPayFeign> {
    @Override
    public JsapiPayFeign create(Throwable cause) {
        return new JsapiPayFeign() {
            @Override
            public JsapiPayResponseDTO createJsapiTrading(JsapiPayDTO jsapiPayDTO) {
                return null;
            }
        };
    }
}
