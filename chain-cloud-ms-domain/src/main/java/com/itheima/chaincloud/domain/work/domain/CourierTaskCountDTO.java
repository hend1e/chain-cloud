package com.itheima.chaincloud.domain.work.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierTaskCountDTO {
    private Long courierId;
    private Long count;
}
