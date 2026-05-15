package com.itheima.chaincloud.domain.work.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

public enum PickupDispatchTaskIsDeletedEnum implements BaseEnum {
    NOT_DELETED(0, "未删除"),
    IS_DELETED(1, "已删除");

    PickupDispatchTaskIsDeletedEnum(Integer code, String value) {
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

    public static PickupDispatchTaskIsDeletedEnum codeOf(Integer code) {
        return EnumUtil.getBy(PickupDispatchTaskIsDeletedEnum::getCode, code);
    }
}
