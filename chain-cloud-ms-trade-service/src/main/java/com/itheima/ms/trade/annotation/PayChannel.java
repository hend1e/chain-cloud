package com.itheima.ms.trade.annotation;

import com.itheima.chaincloud.domain.trade.enums.PayChannelEnum;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PayChannel {

    PayChannelEnum type();

}
