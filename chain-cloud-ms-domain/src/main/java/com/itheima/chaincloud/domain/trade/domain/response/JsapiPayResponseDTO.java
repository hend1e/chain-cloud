package com.itheima.chaincloud.domain.trade.domain.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsapiPayResponseDTO {

    @ApiModelProperty(value = "业务系统订单号")
    private Long productOrderNo;
    @ApiModelProperty(value = "交易系统订单号")
    private Long tradingOrderNo;
    @ApiModelProperty(value = "支付渠道")
    private String tradingChannel;
    @ApiModelProperty(value = "统一下单返回信息，预支付编号")
    private String placeOrderMsg;
    @ApiModelProperty(value = "统一下单返回信息json")
    private String placeOrderJson;
}
