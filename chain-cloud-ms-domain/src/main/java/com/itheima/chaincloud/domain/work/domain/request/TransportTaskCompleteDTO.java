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
@ApiModel("运输任务完成")
public class TransportTaskCompleteDTO {
    @NotNull
    private String transportTaskId;
    @NotNull
    private String transportCertificate;
    @NotNull
    private String deliverPicture;
}
