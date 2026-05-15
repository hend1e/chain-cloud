package com.itheima.chaincloud.domain.work.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

public enum SignRecipientEnum implements BaseEnum {
    OWN(1, "本人"),
    OTHER(2, "他人");

    SignRecipientEnum(Integer code, String value) {
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

    public static SignRecipientEnum codeOf(Integer code) {
        return EnumUtil.getBy(SignRecipientEnum::getCode, code);
    }
}
