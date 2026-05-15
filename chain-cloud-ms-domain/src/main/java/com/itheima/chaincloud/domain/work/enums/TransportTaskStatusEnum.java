package com.itheima.chaincloud.domain.work.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

public enum TransportTaskStatusEnum implements BaseEnum {
    PENDING(1, "待执行"),
    PROCESSING(2, "进行中"),
    CONFIRM(3, "待确认"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");

    TransportTaskStatusEnum(Integer code, String value) {
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
