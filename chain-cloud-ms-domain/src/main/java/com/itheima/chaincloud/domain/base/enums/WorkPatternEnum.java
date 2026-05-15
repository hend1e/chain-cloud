package com.itheima.chaincloud.domain.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum WorkPatternEnum implements BaseEnum {
    /**
     * 1: 礼拜制
     */
    WEEKS((byte) 1, "礼拜制"),
    /**
     * 2: 连续制
     */
    CONTINUITYS((byte) 2, "连续制");

    /**
     * 类型编码
     */
    @EnumValue
    @JsonValue
    @Getter
    private byte type;

    /**
     * 类型描述
     */
    @Getter
    private String desc;


    @Override
    public Integer getCode() {
        return (int) type;
    }

    @Override
    public String getValue() {
        return desc;
    }

    public static String desc(byte type) {
        for (WorkPatternEnum workPatternEnum : values()) {
            if (workPatternEnum.getType() == type) {
                return workPatternEnum.getDesc();
            }
        }
        return null;
    }
}

