package com.itheima.ms.sms.annotation;

import com.itheima.ms.sms.enums.SendChannelEnum;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented //标记注解
public @interface SendChannel {

    SendChannelEnum type();

}
