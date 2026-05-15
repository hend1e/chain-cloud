package com.itheima.chaincloud.domain.user.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 身份证号验证状态
 */
public enum MemberIdCardVerifyStatusEnum implements BaseEnum {
    /**
     * 未验证
     */
    NONE(0, "未验证"),
    /**
     * 验证通过
     */
    SUCCESS(1, "验证通过"),
    /**
     * 验证失败
     */
    FAIL(2, "验证失败");

    MemberIdCardVerifyStatusEnum(Integer code, String value) {
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

    public static MemberIdCardVerifyStatusEnum codeOf(Integer code) {
        return EnumUtil.getBy(MemberIdCardVerifyStatusEnum::getCode, code);
    }

}
