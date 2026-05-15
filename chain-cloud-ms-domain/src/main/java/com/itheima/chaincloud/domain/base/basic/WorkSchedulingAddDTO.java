package com.itheima.chaincloud.domain.base.basic;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排班新增模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("排班新增模型")
public class WorkSchedulingAddDTO {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户类型：1：员工，2：快递员，3:司机
     */
    private Byte userType;

    /**
     * 网点
     */
    private Long agencyId;

    /**
     * 员工名称
     */
    private String name;

    /**
     * 员工编号
     */
    private String employeeNumber;

    /**
     * 电话
     */
    private String phone;

    /**
     * 工作模式id
     */
    private Long workPatternId;

    /**
     * 工作模式类型，1:礼拜制，2：连续制
     */
    private Byte workPatternType;

    /**
     * 操作人
     */
    private Long operator;
}