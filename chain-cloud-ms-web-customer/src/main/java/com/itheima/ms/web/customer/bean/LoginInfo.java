package com.itheima.ms.web.customer.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 登录 IP
     */
    private String loginIP;

    /**
     * 登录设备信息
     */
    private String loginDeviceInfo;

    /**
     * 登录时间
     */
    private String loginTime;

    /**
     * 最后刷新时间
     */
    private String lastRefreshTime;

    /**
     * 刷新次数
     */
    private String refreshCount;

    /**
     * 刷新 IP
     */
    private String refreshIP;

    /**
     * 刷新设备信息
     */
    private String refreshDeviceInfo;
}
