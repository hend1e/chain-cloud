package com.itheima.chaincloud.domain.track.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 轨迹状态
 */
public enum TrackStatusEnum implements BaseEnum {

    NEW(1, "新建"),
    COMPLETE(4, "完成");

    @EnumValue
    @JsonValue
    private Integer code;
    private String value;

    TrackStatusEnum(Integer code, String value) {
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

    public static TrackStatusEnum codeOf(Integer code) {
        return EnumUtil.getBy(TrackStatusEnum::getCode, code);
    }
}
