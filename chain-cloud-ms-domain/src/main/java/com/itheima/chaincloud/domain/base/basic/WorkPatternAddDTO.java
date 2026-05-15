package com.itheima.chaincloud.domain.base.basic;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工作模式新增接口
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("工作模式新增接口")
public class WorkPatternAddDTO {

    /**
     * 用户类型,1:员工，2:快递员，3:司机
     */
    private Byte userType;

    /**
     * 名称
     */
    private String name;

    /**
     * 工作模式：1：礼拜制，2：连续制
     */
    private Byte workPatternType;

    /**
     * 星期一，0:不填充，1:工作，2:休息
     */
    private Byte monday;

    /**
     * 星期二，0:不填充，1:工作，2:休息
     */
    private Byte tuesday;

    /**
     * 星期三，0:不填充，1:工作，2:休息
     */
    private Byte wednesday;

    /**
     * 星期四，0:不填充，1:工作，2:休息
     */
    private Byte thursday;

    /**
     * 星期五，0:不填充，1:工作，2:休息
     */
    private Byte friday;

    /**
     * 星期六，0:不填充，1:工作，2:休息
     */
    private Byte saturday;

    /**
     * 星期日，0:不填充，1:工作，2:休息
     */
    private Byte sunday;

    /**
     * 连续工作天数
     */
    private Integer workDayNum;

    /**
     * 休息天数
     */
    private Integer restDayNum;

    /**
     * 工作开始时间1，分钟数
     */
    private Integer workStartMinute1;

    /**
     * 工作结束时间1，分钟数
     */
    private Integer workEndMinute1;

    /**
     * 操作人
     */
    private Long operator;
}