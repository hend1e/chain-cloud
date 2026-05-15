package com.itheima.chaincloud.domain.user.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 地址薄类型
 */
public enum AddressBookTypeEnum implements BaseEnum {
    /**
     * 寄件
     */
    SEND(1, "寄件"),

    /**
     * 收件
     */
    RECEIPT(2, "收件");

    AddressBookTypeEnum(Integer code, String value) {
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

    public static AddressBookTypeEnum codeOf(Integer code) {
        return EnumUtil.getBy(AddressBookTypeEnum::getCode, code);
    }

}
