package com.itheima.ms.work.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.itheima.chaincloud.common.entity.BaseEntity;
import com.itheima.chaincloud.domain.work.enums.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 取件、派件任务信息表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cc_pickup_dispatch_task")
public class PickupDispatchTaskEntity extends BaseEntity {

    private Long orderId;
    private PickupDispatchTaskTypeEnum taskType;
    private PickupDispatchTaskStatusEnum status;
    private PickupDispatchTaskSignStatusEnum signStatus;
    private SignRecipientEnum signRecipient;
    private Long agencyId;
    private Long courierId;
    private LocalDateTime estimatedStartTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime estimatedEndTime;
    private LocalDateTime actualEndTime;
    private LocalDateTime cancelTime;
    private PickupDispatchTaskAssignedStatusEnum assignedStatus;
    private String mark;
    private PickupDispatchTaskIsDeletedEnum isDeleted;
    private PickupDispatchTaskCancelReasonEnum cancelReason;
    private String cancelReasonDescription;
}
