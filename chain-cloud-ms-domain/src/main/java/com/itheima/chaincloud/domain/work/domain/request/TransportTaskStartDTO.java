package com.itheima.chaincloud.domain.work.domain.request;

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
@ApiModel("运输任务开始")
public class TransportTaskStartDTO {
    @NotNull
    private String transportTaskId;
    @NotNull
    private String cargoPickUpPicture;
    @NotNull
    private String cargoPicture;
}
