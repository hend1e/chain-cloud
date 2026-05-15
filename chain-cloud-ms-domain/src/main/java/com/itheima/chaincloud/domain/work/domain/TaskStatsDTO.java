package com.itheima.chaincloud.domain.work.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 快递员任务统计DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskStatsDTO {
    private Integer totalToday;
    private Integer completed;
    private Integer inProgress;
    private Integer pendingPickup;
    private Integer pendingDelivery;
    private List<Long> taskIds = List.of();
    private Boolean isIdle;
    private Long idleDurationMin;
}
