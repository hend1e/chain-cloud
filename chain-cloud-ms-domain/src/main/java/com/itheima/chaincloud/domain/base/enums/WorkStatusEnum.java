package com.itheima.chaincloud.domain.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum WorkStatusEnum implements BaseEnum {
    NOMAL((byte) 1, "正常"), STOPING((byte) 2, "停用");

    /**
     * 状态编码
     */
    @EnumValue
    @JsonValue
    @Getter
    private byte status;

    /**
     * 状态描述
     */
    @Getter
    private String desc;


    @Override
    public Integer getCode() {
        return (int) status;
    }

    @Override
    public String getValue() {
        return desc;
    }

    public static String desc(byte status) {
        for (WorkStatusEnum workStatusEnum : values()) {
            if (status == workStatusEnum.getStatus()) {
                return workStatusEnum.getDesc();
            }
        }
        return null;
    }
}
