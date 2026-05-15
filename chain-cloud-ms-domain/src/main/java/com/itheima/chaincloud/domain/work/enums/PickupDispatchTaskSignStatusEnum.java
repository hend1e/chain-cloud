package com.itheima.chaincloud.domain.work.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

public enum PickupDispatchTaskSignStatusEnum implements BaseEnum {
    NOT_SIGNED(0, "未签收"),
    RECEIVED(1, "已签收"),
    REJECTION(2, "拒收");

    PickupDispatchTaskSignStatusEnum(Integer code, String value) {
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

    public static PickupDispatchTaskSignStatusEnum codeOf(Integer code) {
        return EnumUtil.getBy(PickupDispatchTaskSignStatusEnum::getCode, code);
    }
}
