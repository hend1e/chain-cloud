package com.itheima.chaincloud.domain.courier.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 快递员当前状态信息DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourierStatusDTO {
    private Long courierId;
    private String status;
    private LocationInfo location;
    private TaskStats tasks;
    private AvailabilityInfo availability;
    private CapacityInfo capacity;
    private RouteInfo route;
    private PerformanceInfo performance;
    private Long dataFreshnessMs;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationInfo {
        private Double lat;
        private Double lng;
        private String address;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TaskStats {
        private Integer totalToday;
        private Integer completed;
        private Integer inProgress;
        private Integer pendingPickup;
        private Integer pendingDelivery;
        private List<Long> taskIds;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AvailabilityInfo {
        private Boolean isIdle;
        private Long idleDurationMin;
        private LocalDateTime nextAvailableTime;
        private Long todayWorkingDurationMin;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CapacityInfo {
        private Integer currentLoad;
        private Integer maxLoad;
        private Double loadRatio;
        private String vehicleType;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RouteInfo {
        private String routeId;
        private String nextStop;
        private Integer etaNextStopMin;
        private Integer remainingDistanceMeters;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PerformanceInfo {
        private Double onTimeRate;
        private Integer avgDeliveryTimeMin;
        private Integer complaintCount;
        private Double rating;
    }
}
