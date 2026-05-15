package com.itheima.chaincloud.domain.work.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itheima.chaincloud.domain.work.enums.TransportTaskAssignedStatusEnum;
import com.itheima.chaincloud.domain.work.enums.TransportTaskLoadingStatusEnum;
import com.itheima.chaincloud.domain.work.enums.TransportTaskStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportTaskDTO {
    private Long id;
    private Long truckPlanId;
    private Long transportTripsId;
    private Long startAgencyId;
    private Long endAgencyId;
    private TransportTaskStatusEnum status;
    private TransportTaskAssignedStatusEnum assignedStatus;
    private TransportTaskLoadingStatusEnum loadingStatus;
    private Long truckId;
    private String cargoPickUpPicture;
    private String cargoPicture;
    private String transportCertificate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planDepartureTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualDepartureTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planArrivalTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualArrivalTime;
    private String deliverPicture;
    private String deliveryLatitude;
    private String deliveryLongitude;
    private String deliverLatitude;
    private String deliverLongitude;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private List<String> transportOrderIds;
    private Integer transportOrderCount;
    private Integer page;
    private Integer pageSize;
    private List<Long> ids;
    private Double distance;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;
}
