package com.itheima.chaincloud.domain.trade.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 退款状态枚举
 */
public enum RefundStatusEnum implements BaseEnum {

    SENDING(1, "退款中"),
    SUCCESS(2, "成功"),
    FAIL(3, "失败");

    @EnumValue
    @JsonValue
    private Integer code;
    private String value;

    RefundStatusEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public static RefundStatusEnum codeOf(Integer code) {
        return EnumUtil.getBy(RefundStatusEnum::getCode, code);
    }
}
