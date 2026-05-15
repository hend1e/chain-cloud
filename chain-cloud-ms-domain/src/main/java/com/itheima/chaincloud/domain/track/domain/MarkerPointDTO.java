package com.itheima.chaincloud.domain.track.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkerPointDTO {
    @ApiModelProperty("经度")
    private Double lng;
    @ApiModelProperty("纬度")
    private Double lat;
}
