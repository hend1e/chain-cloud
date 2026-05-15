package com.itheima.chaincloud.domain.aigc.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParcelItemCheckResponse {

    @ApiModelProperty(value = "物品名称", required = true)
    private String itemName;

    @ApiModelProperty(value = "分类 ID", required = true)
    private Long categoryId;

    @ApiModelProperty(value = "分类名称", required = true)
    private String categoryName;

    @ApiModelProperty(value = "是否禁寄物品", required = true)
    private Boolean isProhibited;

    @ApiModelProperty(value = "判断原因", required = true)
    private String reason;
}
