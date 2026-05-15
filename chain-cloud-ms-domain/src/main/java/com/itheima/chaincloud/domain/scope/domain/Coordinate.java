package com.itheima.chaincloud.domain.scope.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coordinate {
    private Double longitude;
    private Double latitude;
}
