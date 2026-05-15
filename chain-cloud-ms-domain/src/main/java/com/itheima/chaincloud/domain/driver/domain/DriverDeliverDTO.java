package com.itheima.chaincloud.domain.driver.domain;

import io.swagger.annotations.ApiModel;
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
@ApiModel("司机交付对象")
public class DriverDeliverDTO {
    @NotNull
    @ApiModelProperty(value = "司机作业id", required = true)
    private String id;

    @NotNull
    @ApiModelProperty(value = "交付凭证,多个图片url以逗号分隔", required = true)
    private String transportCertificate;

    @NotNull
    @ApiModelProperty(value = "交付图片,多个图片url以逗号分隔", required = true)
    private String deliverPicture;
}
