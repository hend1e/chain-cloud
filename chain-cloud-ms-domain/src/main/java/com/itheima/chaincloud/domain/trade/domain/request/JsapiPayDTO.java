package com.itheima.chaincloud.domain.trade.domain.request;

import com.itheima.chaincloud.domain.trade.enums.PayChannelEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsapiPayDTO {

    @ApiModelProperty(value = "openId标识", required = true)
    private String openId;
    @ApiModelProperty(value = "商户号", required = true)
    private Long enterpriseId;
    @ApiModelProperty(value = "业务系统订单号", required = true)
    private Long productOrderNo;
    @ApiModelProperty(value = "支付渠道", required = true)
    private PayChannelEnum tradingChannel;
    @ApiModelProperty(value = "交易金额，单位：元", required = true)
    private BigDecimal tradingAmount;
    @ApiModelProperty(value = "备注，如：运费", required = true)
    private String memo;
}
