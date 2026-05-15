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
public class NativePayResponseDTO {

    @ApiModelProperty(value = "二维码base64数据")
    private String qrCode;
    @ApiModelProperty(value = "业务系统订单号")
    private Long productOrderNo;
    @ApiModelProperty(value = "交易系统订单号")
    private Long tradingOrderNo;
    @ApiModelProperty(value = "支付渠道")
    private String tradingChannel;
}
