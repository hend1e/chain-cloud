package com.itheima.chaincloud.domain.base.basic;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工作模式
 */
@ApiModel("工作模式")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkPatternDTO {

    /**
     * 工作模式id
     */
    private Long id;

    /**
     * 工作模式名称
     */
    private String name;

    /**
     * 工作模式类型描述
     */
    private String workPatternTypeDesc;

    /**
     * 工作模式类型描述
     */
    private Byte workPatternType;

    /**
     * 工作日期
     */
    private String workDate;

    /**
     * 用户类型
     */
    private Byte userType;

    /**
     * 星期一
     */
    private Byte monday;

    /**
     * 星期二
     */
    private Byte tuesday;

    /**
     * 星期三
     */
    private Byte wednesday;

    /**
     * 星期四
     */
    private Byte thursday;

    /**
     * 星期五
     */
    private Byte friday;

    /**
     * 星期六
     */
    private Byte saturday;

    /**
     * 星期日
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
     * 状态
     */
    private Byte status;

    /**
     * 工作开始时间1
     */
    private Integer workStartMinute1;

    /**
     * 工作结束时间1
     */
    private Integer workEndMinute1;

    /**
     * 创建者
     */
    private Long creater;

    /**
     * 更新者
     */
    private Long updater;

    /**
     * 是否删除
     */
    private Boolean isDelete;
}