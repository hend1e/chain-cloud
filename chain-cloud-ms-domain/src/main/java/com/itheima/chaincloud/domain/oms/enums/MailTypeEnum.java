package com.itheima.chaincloud.domain.oms.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 订单类型枚举
 */
public enum MailTypeEnum implements BaseEnum {

    /**
     * 寄件
     */
    SEND(1, "寄件"),

    /**
     * 收件
     */
    RECEIVE(2, "收件"),

    ALL(3, "全部");

    MailTypeEnum(Integer code, String value) {
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

    public static MailTypeEnum codeOf(Integer code) {
        return EnumUtil.getBy(MailTypeEnum::getCode, code);
    }
}
