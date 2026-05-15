package com.itheima.chaincloud.domain.courier.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierScopeDTO {
    private Long id;
    private Long userId;
    private Long areaId;
    private List<List<Map>> mutiPoints;
}
