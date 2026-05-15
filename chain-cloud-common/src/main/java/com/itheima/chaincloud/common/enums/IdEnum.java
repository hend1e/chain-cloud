package com.itheima.chaincloud.common.enums;

import lombok.Getter;

/**
 * @author zzj
 * @version 1.0
 */
public enum IdEnum implements BaseEnum {

    TRANSPORT_ORDER(1, "运单号", "transport_order", "segment", "CC");

    private final Integer code;
    private final String value;
    @Getter
    private final String biz; //业务名称
    @Getter
    private final String type; //类型：自增长（segment），雪花id（snowflake）
    @Getter
    private final String prefix;//id前缀

    IdEnum(Integer code, String value, String biz, String type, String prefix) {
        this.code = code;
        this.value = value;
        this.biz = biz;
        this.type = type;
        this.prefix = prefix;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "IdEnum{" + "code=" + code +
                ", value='" + value + '\'' +
                ", biz='" + biz + '\'' +
                ", type='" + type + '\'' +
                ", prefix='" + prefix + '\'' +
                '}';
    }
}
