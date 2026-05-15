package com.itheima.chaincloud.domain.track.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 轨迹类型枚举
 */
public enum TrackTypeEnum implements BaseEnum {
    DRIVER(2, "司机轨迹"),
    COURIER(3, "快递员轨迹");

    @EnumValue
    @JsonValue
    private Integer code;
    private String value;

    TrackTypeEnum(Integer code, String value) {
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

    public static TrackTypeEnum codeOf(Integer code) {
        return EnumUtil.getBy(TrackTypeEnum::getCode, code);
    }
}
