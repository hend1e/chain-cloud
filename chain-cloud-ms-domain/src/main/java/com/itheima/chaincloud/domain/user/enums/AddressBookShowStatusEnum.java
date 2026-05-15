package com.itheima.chaincloud.domain.user.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 地址薄展示状态
 */
public enum AddressBookShowStatusEnum implements BaseEnum {
    /**
     * 展示
     */
    SHOW_STATUS(1, "展示"),

    /**
     * 不展示
     */
    NOT_SHOW_STATUS(0, "不展示");

    AddressBookShowStatusEnum(Integer code, String value) {
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

    public static AddressBookShowStatusEnum codeOf(Integer code) {
        return EnumUtil.getBy(AddressBookShowStatusEnum::getCode, code);
    }

}
