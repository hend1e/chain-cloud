package com.itheima.chaincloud.domain.oms.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 订单类型枚举
 */
public enum OrderPickupTypeEnum implements BaseEnum {

    /**
     * 网点自寄
     */
    NO_PICKUP(1, "网点自寄"),

    /**
     * 上门取件
     */
    PICKUP(2, "上门取件");

    OrderPickupTypeEnum(Integer code, String value) {
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

    public static OrderPickupTypeEnum codeOf(Integer code) {
        return EnumUtil.getBy(OrderPickupTypeEnum::getCode, code);
    }
}
