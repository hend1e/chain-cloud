package com.itheima.chaincloud.domain.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/***
 * @author wangxiaobing
 *
 * 车辆计划运行状态枚举
 */
public enum TruckPlanStatusEnum implements BaseEnum {
    /**
     * 已到达
     */
    ARRIVED(2, "ARRIVED"),

    /**
     * 禁用
     */
    DISABLED(0, "DISABLED"),

    /**
     * 正常
     */
    NORMAL(1, "NORMAL");

    TruckPlanStatusEnum(Integer code, String value) {
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
