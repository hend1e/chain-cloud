package com.itheima.chaincloud.domain.driver.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 司机任务状态
 */
public enum DriverJobStatusEnum implements BaseEnum {
    PENDING(1, "待执行"),
    PROCESSING(2, "进行中"),
    CONFIRM(3, "改派"),
    DELIVERED(4, "已交付"),
    CANCELLED(5, "已作废"),
    COMPLETED(6, "已完成");

    DriverJobStatusEnum(Integer code, String value) {
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

    public static DriverJobStatusEnum codeOf(Integer code) {
        return EnumUtil.getBy(DriverJobStatusEnum::getCode, code);
    }
}
