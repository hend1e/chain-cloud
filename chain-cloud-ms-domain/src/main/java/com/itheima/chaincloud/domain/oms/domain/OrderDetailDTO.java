package com.itheima.chaincloud.domain.oms.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {

    /**
     * 订单
     */
    private OrderDTO orderDTO;

    /**
     * 货物
     */
    private OrderLocationDTO orderLocationDTO;
}
