package com.itheima.ms.web.manager.config;

import com.itheima.chaincloud.common.config.FeignErrorDecoder;
import com.itheima.chaincloud.common.exception.CCWebException;
import org.springframework.context.annotation.Configuration;

/**
 * web调用feign失败解码器实现
 *
 * @author zzj
 * @version 1.0
 */
@Configuration
public class WebFeignErrorDecoder extends FeignErrorDecoder {

    @Override
    public Exception call(int status, int code, String msg) {
        return new CCWebException(msg);
    }
}
