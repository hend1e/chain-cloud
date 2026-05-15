package com.itheima.chaincloud.domain.work.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

public enum TransportOrderStatusEnum implements BaseEnum {
    CREATED(1, "新建"),
    LOADED(2, "已装车"),
    PROCESSING(3, "运输中"),
    ARRIVED_END(4, "到达终端网点"),
    REJECTED(6, "拒收");

    TransportOrderStatusEnum(Integer code, String value) {
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
