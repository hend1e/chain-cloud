package com.itheima.chaincloud.domain.oms.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 订单类型枚举
 */
public enum OrderTypeEnum implements BaseEnum {

    /**
     * 同城订单
     */
    INCITY(1, "同城订单"),

    /**
     * 城际订单
     */
    OUTCITY(2, "城际订单");

    OrderTypeEnum(Integer code, String value) {
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

    public static OrderTypeEnum codeOf(Integer code) {
        return EnumUtil.getBy(OrderTypeEnum::getCode, code);
    }
}
