package com.itheima.chaincloud.domain.oms.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 订单类型枚举
 */
public enum OrderPaymentMethodEnum implements BaseEnum {

    /**
     * 预结
     */
    PRE_PAY(1, "预结"),

    /**
     * 到付
     */
    END_PAY(2, "到付");

    OrderPaymentMethodEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    /**
     * 类型编码
     */
    @EnumValue
    @JsonValue
    private final Integer code;

    /**
     * 类型值
     */
    private final String value;

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getValue() {
        return value;
    }

    public static OrderPaymentMethodEnum codeOf(Integer code) {
        return EnumUtil.getBy(OrderPaymentMethodEnum::getCode, code);
    }
}
