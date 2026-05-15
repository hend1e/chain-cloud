package com.itheima.chaincloud.domain.work.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

public enum TransportTaskLoadingStatusEnum implements BaseEnum {
    HALF(1, "半载"),
    FULL(2, "满载"),
    EMPTY(3, "空载");

    TransportTaskLoadingStatusEnum(Integer code, String value) {
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
