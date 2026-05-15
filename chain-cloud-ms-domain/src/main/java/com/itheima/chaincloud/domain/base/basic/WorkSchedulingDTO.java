package com.itheima.chaincloud.domain.base.basic;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 排班分页查询
 */
@ApiModel("排班分页查询")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSchedulingDTO {

    /**
     * 工作排班信息id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 员工账号
     */
    private String employeeNumber;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 工作计划
     */
    private List<Boolean> workSchedules;

    /**
     * 开始上班时间1
     */
    private Integer workStartMinute1;

    /**
     * 结束下班时间1
     */
    private Integer workEndMinute1;

    /**
     * 工作模式名称
     */
    private String workPatternName;

    /**
     * 用户类型：1：员工，2：快递员，3:司机
     */
    private Byte userType;

    /**
     * 工作模式id
     */
    private Long workPatternId;

    /**
     * 网点名称
     */
    private String agencyName;

    /**
     * 网点
     */
    private Long agencyId;
}