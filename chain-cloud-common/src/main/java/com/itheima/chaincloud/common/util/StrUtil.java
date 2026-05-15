package com.itheima.chaincloud.common.util;

/**
 * 继承自hutool的StrUtil，做了一些自定义的扩展
 */
public class StrUtil extends cn.hutool.core.util.StrUtil {

    public static boolean notEquals(CharSequence str1, CharSequence str2) {
        return !equals(str1, str2);
    }

}
