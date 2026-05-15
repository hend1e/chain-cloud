package com.itheima.chaincloud.domain.driver.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itheima.chaincloud.domain.driver.enums.DriverJobStatusEnum;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("司机作业单")
public class DriverJobDTO {
    private Long id;
    private Long startAgencyId;
    private Long endAgencyId;
    private DriverJobStatusEnum status;
    private Long driverId;
    private Long transportTaskId;
    private String startHandover;
    private String finishHandover;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planDepartureTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualDepartureTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planArrivalTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualArrivalTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    private Boolean enablePickUp = false;
}
