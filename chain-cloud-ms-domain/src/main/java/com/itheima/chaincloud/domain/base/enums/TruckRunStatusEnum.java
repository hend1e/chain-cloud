package com.itheima.chaincloud.domain.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/***
 * @author wangxiaobing
 *
 * 车辆运行状态枚举
 */
public enum TruckRunStatusEnum implements BaseEnum {
    /**
     * 已达到
     */
    ARRIVED(1, "ARRIVED"),

    /**
     * 运输中
     */
    RUNNING(0, "RUNNING");

    TruckRunStatusEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    /**
     * 类型编码
     */
    @EnumValue
    @JsonValue
    private final Integer code;

    /**
     * 类型值
     */
    private final String value;


    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getValue() {
        return value;
    }
}
