package com.itheima.chaincloud.api.trade;

import com.itheima.chaincloud.api.trade.fallback.TradingFeignFallbackFactory;
import com.itheima.chaincloud.domain.trade.domain.RefundRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "chain-cloud-ms-trade", contextId = "RefundRecord", path = "/refund/records", fallbackFactory = TradingFeignFallbackFactory.class)
public interface RefundRecordFeign {

    @GetMapping
    List<RefundRecordDTO> findList(@RequestParam(value = "productOrderNo", required = false) Long productOrderNo,
                                    @RequestParam(value = "tradingOrderNo", required = false) Long tradingOrderNo);
}
