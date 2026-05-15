package com.itheima.chaincloud.domain.work.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderToTransportOrderDTO {
    private String id;
    private Long orderId;
    private LocalDateTime created;
}
