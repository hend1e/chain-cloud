package com.itheima.chaincloud.api.trade;

import com.itheima.chaincloud.api.trade.fallback.NativePayFeignFallbackFactory;
import com.itheima.chaincloud.domain.trade.domain.request.NativePayDTO;
import com.itheima.chaincloud.domain.trade.domain.response.NativePayResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "chain-cloud-ms-trade", contextId = "NativePay", path = "/native", fallbackFactory = NativePayFeignFallbackFactory.class)
public interface NativePayFeign {

    @PostMapping
    NativePayResponseDTO createDownLineTrading(@RequestBody NativePayDTO nativePayDTO);

    @GetMapping("qrcode/{tradingOrderNo}")
    String queryQrCode(@PathVariable("tradingOrderNo") Long tradingOrderNo);
}
