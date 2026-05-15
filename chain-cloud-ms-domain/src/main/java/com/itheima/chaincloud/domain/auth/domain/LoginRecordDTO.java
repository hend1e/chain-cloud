package com.itheima.chaincloud.domain.auth.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录记录表 DTO
 */
@Data
public class LoginRecordDTO {

    /**
     * 登录记录 id
     */
    @ApiModelProperty(value = "登录记录 id")
    private Long id;

    /**
     * 登录账号
     */
    @ApiModelProperty(value = "登录账号")
    private String account;

    /**
     * 用户类型，1-管理员 2-快递员 3-司机 4-用户
     */
    @ApiModelProperty(value = "用户类型，1-管理员 2-快递员 3-司机 4-用户")
    private Integer userType;

    /**
     * 登录 IP 地址
     */
    @ApiModelProperty(value = "登录 IP 地址")
    private String loginIp;

    /**
     * 设备信息（浏览器或设备标识）
     */
    @ApiModelProperty(value = "设备信息（浏览器或设备标识）")
    private String deviceInfo;

    /**
     * 登录状态，0-失败 1-成功
     */
    @ApiModelProperty(value = "登录状态，0-失败 1-成功")
    private Integer loginStatus;

    /**
     * 登录失败原因
     */
    @ApiModelProperty(value = "登录失败原因")
    private String failReason;

    /**
     * 登录时间
     */
    @ApiModelProperty(value = "登录时间")
    private LocalDateTime loginTime;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime created;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updated;

}
