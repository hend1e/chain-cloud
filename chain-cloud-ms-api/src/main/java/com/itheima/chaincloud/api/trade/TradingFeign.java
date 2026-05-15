package com.itheima.chaincloud.api.trade;

import com.itheima.chaincloud.api.trade.fallback.TradingFeignFallbackFactory;
import com.itheima.chaincloud.domain.trade.domain.TradingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "chain-cloud-ms-trade", contextId = "Trading", path = "/tradings", fallbackFactory = TradingFeignFallbackFactory.class)
public interface TradingFeign {

    @GetMapping
    TradingDTO queryTrading(@RequestParam(value = "productOrderNo", required = false) Long productOrderNo,
                            @RequestParam(value = "tradingOrderNo", required = false) Long tradingOrderNo);
}
