package com.itheima.chaincloud.domain.courier.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 取派件任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskPickupDispatchDTO {
    private Long id;
    private Long orderId;
    private Integer taskType;
    private Integer status;
    private Integer signStatus;
    private Long agencyId;
    private Long courierId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedEndTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
    private Integer assignedStatus;
    private String mark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Integer page;
    private Integer pageSize;
    private String senderProvinceId;
    private String senderCityId;
    private String senderName;
    private String receiverProvinceId;
    private String receiverCityId;
    private String receiverName;
    private List<Long> ids;
    private List<Long> orderIds;
}
