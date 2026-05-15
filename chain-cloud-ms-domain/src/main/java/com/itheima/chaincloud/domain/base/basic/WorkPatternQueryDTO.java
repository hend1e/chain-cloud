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
public class WorkPatternQueryDTO {

    /**
     * 页数，默认第一页
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}