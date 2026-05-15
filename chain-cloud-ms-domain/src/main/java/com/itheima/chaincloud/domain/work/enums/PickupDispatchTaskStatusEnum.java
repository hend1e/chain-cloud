package com.itheima.chaincloud.domain.work.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

public enum PickupDispatchTaskStatusEnum implements BaseEnum {
    NEW(1, "新任务"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消");

    PickupDispatchTaskStatusEnum(Integer code, String value) {
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

    public static PickupDispatchTaskStatusEnum codeOf(Integer code) {
        return EnumUtil.getBy(PickupDispatchTaskStatusEnum::getCode, code);
    }
}
