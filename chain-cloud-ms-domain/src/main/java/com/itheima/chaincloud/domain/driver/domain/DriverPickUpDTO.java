package com.itheima.chaincloud.domain.driver.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverPickUpDTO {
    @NotNull
    @ApiModelProperty(value = "司机作业id", required = true)
    private String id;

    @NotNull
    @ApiModelProperty(value = "提货凭证,多个图片url以逗号分隔", required = true)
    private String cargoPickUpPicture;

    @NotNull
    @ApiModelProperty(value = "货物照片,多个图片url以逗号分隔", required = true)
    private String cargoPicture;
}
