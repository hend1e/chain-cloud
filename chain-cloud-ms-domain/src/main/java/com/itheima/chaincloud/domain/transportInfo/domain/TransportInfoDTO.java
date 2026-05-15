package com.itheima.chaincloud.domain.transportInfo.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportInfoDTO {
    @ApiModelProperty(value = "运单id", required = true)
    private String transportOrderId;

    @ApiModelProperty(value = "info信息列表", required = true)
    private List<TransportInfoDetailDTO> infoList;
}
