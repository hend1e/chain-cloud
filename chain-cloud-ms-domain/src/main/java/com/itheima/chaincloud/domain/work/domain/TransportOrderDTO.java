package com.itheima.chaincloud.domain.work.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itheima.chaincloud.domain.work.enums.TransportOrderSchedulingStatusEnum;
import com.itheima.chaincloud.domain.work.enums.TransportOrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportOrderDTO {
    private String id;
    private Long orderId;
    private TransportOrderStatusEnum status;
    private TransportOrderSchedulingStatusEnum schedulingStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;
    private Long startAgencyId;
    private Long endAgencyId;
    private Long currentAgencyId;
    private Long nextAgencyId;
    private String transportLine;
    private BigDecimal totalVolume;
    private BigDecimal totalWeight;
    private Boolean isRejection = false;
}
