package com.itheima.ms.work.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.itheima.chaincloud.common.entity.BaseEntity;
import com.itheima.chaincloud.domain.work.enums.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 运输任务表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cc_transport_task")
public class TransportTaskEntity extends BaseEntity {

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
    private LocalDateTime planDepartureTime;
    private LocalDateTime actualDepartureTime;
    private LocalDateTime planArrivalTime;
    private LocalDateTime actualArrivalTime;
    private String deliverPicture;
    private String deliveryLatitude;
    private String deliveryLongitude;
    private String deliverLatitude;
    private String deliverLongitude;
    private String mark;
    private Double distance;
}
