package com.itheima.chaincloud.domain.oms.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 订单支付状态枚举
 */
public enum OrderPaymentStatusEnum implements BaseEnum {

    /**
     * 未支付
     */
    UNPAID(1, "未支付"),

    /**
     * 已支付
     */
    PAID(2, "已支付");

    OrderPaymentStatusEnum(Integer code, String value) {
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

    public static OrderPaymentStatusEnum codeOf(Integer code) {
        return EnumUtil.getBy(OrderPaymentStatusEnum::getCode, code);
    }
}
