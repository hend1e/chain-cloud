package com.itheima.chaincloud.domain.trade.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 支付渠道枚举
 */
public enum PayChannelEnum implements BaseEnum {

    ALI_PAY(1, "支付宝"),
    WECHAT_PAY(2, "微信支付");

    @EnumValue
    @JsonValue
    private Integer code;
    private String value;

    PayChannelEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public static PayChannelEnum codeOf(Integer code) {
        return EnumUtil.getBy(PayChannelEnum::getCode, code);
    }
}
