package com.itheima.chaincloud.domain.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/***
 * @author wangxiaobing
 *
 * 车次周期枚举
 */
public enum TruckTripsPeriodEnum implements BaseEnum {
    /**
     * 天
     */
    DAY(1, "DAY"),

    /**
     * 周
     */
    WEEK(2, "WEEK"),

    /**
     * 月
     */
    MONTH(3, "MONTH");


    TruckTripsPeriodEnum(Integer code, String value) {
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
