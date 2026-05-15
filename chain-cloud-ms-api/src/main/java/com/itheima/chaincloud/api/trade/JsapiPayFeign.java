package com.itheima.chaincloud.api.trade;

import com.itheima.chaincloud.api.trade.fallback.JsapiPayFeignFallbackFactory;
import com.itheima.chaincloud.domain.trade.domain.request.JsapiPayDTO;
import com.itheima.chaincloud.domain.trade.domain.response.JsapiPayResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "chain-cloud-ms-trade", contextId = "JsapiPay", path = "/jsapi", fallbackFactory = JsapiPayFeignFallbackFactory.class)
public interface JsapiPayFeign {

    @PostMapping
    JsapiPayResponseDTO createJsapiTrading(@RequestBody JsapiPayDTO jsapiPayDTO);
}
