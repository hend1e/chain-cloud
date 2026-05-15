package com.itheima.chaincloud.domain.work.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

public enum TransportOrderSchedulingStatusEnum implements BaseEnum {
    TO_BE_SCHEDULED(1, "待调度"),
    NO_MATCH_TRANSPORT_LINE(2, "未匹配到线路"),
    SCHEDULED(3, "已调度");

    TransportOrderSchedulingStatusEnum(Integer code, String value) {
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
}
