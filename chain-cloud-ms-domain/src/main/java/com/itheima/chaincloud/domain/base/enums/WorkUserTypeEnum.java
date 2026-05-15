package com.itheima.chaincloud.domain.base.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 1:员工，2：快递员，3：司机
 */

public enum WorkUserTypeEnum implements BaseEnum {
    /**
     * 1:员工
     */
    USER(1, "员工"),
    /**
     * 2：快递员
     */
    COURIER(2, "快递员"),
    /**
     * 3：司机
     */
    DRIVER(3, "司机");


    WorkUserTypeEnum(Integer code, String value) {
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
        return this.code;
    }

    @Override
    public String getValue() {
        return value;
    }


    public static WorkUserTypeEnum codeOf(Integer code) {
        return EnumUtil.getBy(WorkUserTypeEnum::getCode, code);
    }

}
