package com.itheima.chaincloud.common.exception;

import com.itheima.chaincloud.common.enums.BaseEnum;
import com.itheima.chaincloud.common.enums.BaseExceptionEnum;
import lombok.Data;

/**
 * 自定义异常
 */
@Data
public class CCException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String msg; //异常中的信息
    private int code = 1001; //业务状态码，规则：4位数，从1001开始递增
    private int status = 500; //http状态码，按照http协议规范，如：200,201,400等

    public CCException(BaseEnum baseEnum) {
        super(baseEnum.getValue());
        this.msg = baseEnum.getValue();
        this.code = baseEnum.getCode();
    }

    public CCException(BaseEnum baseEnum, Throwable e) {
        super(baseEnum.getValue(), e);
        this.msg = baseEnum.getValue();
        this.code = baseEnum.getCode();
    }

    public CCException(BaseExceptionEnum errorEnum) {
        super(errorEnum.getValue());
        this.status = errorEnum.getStatus();
        this.msg = errorEnum.getValue();
        this.code = errorEnum.getCode();
    }

    public CCException(BaseExceptionEnum errorEnum, Throwable e) {
        super(errorEnum.getValue(), e);
        this.status = errorEnum.getStatus();
        this.msg = errorEnum.getValue();
        this.code = errorEnum.getCode();
    }

    public CCException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public CCException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public CCException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public CCException(String msg, int code, int status) {
        super(msg);
        this.msg = msg;
        this.code = code;
        this.status = status;
    }

    public CCException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }

    public CCException(String msg, int code, int status, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
        this.status = status;
    }

}
