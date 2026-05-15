package com.itheima.chaincloud.common.config;

import com.itheima.chaincloud.common.exception.CCException;

/**
 * 通用解码器实现
 *
 * @author zzj
 * @version 1.0
 */
public class CommonFeignErrorDecoder extends FeignErrorDecoder {

    @Override
    public Exception call(int status, int code, String msg) {
        return new CCException(msg, code, status);
    }
}
