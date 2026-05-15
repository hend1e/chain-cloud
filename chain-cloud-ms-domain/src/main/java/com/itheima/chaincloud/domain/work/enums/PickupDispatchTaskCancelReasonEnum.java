package com.itheima.chaincloud.domain.work.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

public enum PickupDispatchTaskCancelReasonEnum implements BaseEnum {
    MALICIOUS_ORDER(1),
    CANCEL_BY_USER(2),
    FORBIDDEN_GOODS(3),
    REPEAT_ORDER(4),
    UNABLE_TO_CONTACT(5),
    FALSE_ADDRESS(6),
    ADDRESS_MISMATCH(7),
    RETURN_TO_AGENCY(8);

    PickupDispatchTaskCancelReasonEnum(Integer code) {
        this.code = code;
        this.value = "";
    }

    PickupDispatchTaskCancelReasonEnum(Integer code, String value) {
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

    public static PickupDispatchTaskCancelReasonEnum codeOf(Integer code) {
        return EnumUtil.getBy(PickupDispatchTaskCancelReasonEnum::getCode, code);
    }
}
