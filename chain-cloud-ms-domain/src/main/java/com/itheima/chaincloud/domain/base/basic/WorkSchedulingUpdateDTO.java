package com.itheima.chaincloud.domain.base.basic;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排班修改模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("排班修改模型")
public class WorkSchedulingUpdateDTO {

    /**
     * 排班id
     */
    private Long id;

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