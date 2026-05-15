package com.itheima.chaincloud.domain.work.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskAssignedStatusEnum;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskCancelReasonEnum;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskIsDeletedEnum;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskSignStatusEnum;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskStatusEnum;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskTypeEnum;
import com.itheima.chaincloud.domain.work.enums.SignRecipientEnum;
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
public class PickupDispatchTaskDTO {
    private Long id;
    private Long orderId;
    private PickupDispatchTaskTypeEnum taskType;
    private PickupDispatchTaskStatusEnum status;
    private PickupDispatchTaskSignStatusEnum signStatus;
    private SignRecipientEnum signRecipient;
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
    private PickupDispatchTaskAssignedStatusEnum assignedStatus;
    private String mark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;
    private PickupDispatchTaskIsDeletedEnum isDeleted;
    private PickupDispatchTaskCancelReasonEnum cancelReason;
    private String cancelReasonDescription;
    private List<Long> ids;
    private List<Long> orderIds;
}
