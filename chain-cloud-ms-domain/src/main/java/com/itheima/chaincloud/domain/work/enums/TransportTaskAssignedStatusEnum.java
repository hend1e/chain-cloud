package com.itheima.chaincloud.domain.work.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

public enum TransportTaskAssignedStatusEnum implements BaseEnum {
    DISTRIBUTED(2, "已分配"),
    MANUAL_DISTRIBUTED(3, "待人工分配");

    TransportTaskAssignedStatusEnum(Integer code, String value) {
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
