package com.itheima.chaincloud.domain.work.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

public enum PickupDispatchTaskTypeEnum implements BaseEnum {
    PICKUP(1, "取件任务"),
    DISPATCH(2, "派件任务");

    PickupDispatchTaskTypeEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String value;

    @Override
    public Integer getCode() { return code; }

    @Override
    public String getValue() { return value; }

    public static PickupDispatchTaskTypeEnum codeOf(Integer code) {
        return EnumUtil.getBy(PickupDispatchTaskTypeEnum::getCode, code);
    }
}
