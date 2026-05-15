package com.itheima.ms.work.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.itheima.chaincloud.domain.work.enums.TransportOrderSchedulingStatusEnum;
import com.itheima.chaincloud.domain.work.enums.TransportOrderStatusEnum;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运单表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cc_transport_order")
public class TransportOrderEntity implements Serializable {

    @TableId
    private String id;

    private Long orderId;
    private TransportOrderStatusEnum status;
    private TransportOrderSchedulingStatusEnum schedulingStatus;
    private Long startAgencyId;
    private Long endAgencyId;
    private Long currentAgencyId;
    private Long nextAgencyId;
    private String transportLine;
    private BigDecimal totalVolume;
    private BigDecimal totalWeight;
    private Boolean isRejection;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updated;
}
