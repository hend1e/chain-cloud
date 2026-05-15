package com.itheima.ms.work.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.itheima.chaincloud.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 运单与运输任务关联表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cc_transport_order_task")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportOrderTaskEntity extends BaseEntity {

    private String transportOrderId;
    private Long transportTaskId;
}
