package com.itheima.ms.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.itheima.chaincloud.common.entity.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 登录记录表
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName("cc_login_record")
public class LoginRecordEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 登录账号
     */
    private String account;

    /**
     * 用户类型，1-管理员 2-快递员 3-司机 4-用户
     */
    private Integer userType;

    /**
     * 登录 IP 地址
     */
    private String loginIp;

    /**
     * 设备信息（浏览器或设备标识）
     */
    private String deviceInfo;

    /**
     * 登录状态，0-失败 1-成功
     */
    private Integer loginStatus;

    /**
     * 登录失败原因
     */
    private String failReason;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

}
