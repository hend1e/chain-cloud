package com.itheima.ms.dispatch.job;

import com.itheima.ms.dispatch.dto.DispatchMsgDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DispatchContext {

    /**
     * 已选订单
     */
    private List<DispatchMsgDTO> selectedOrders = List.of();

    /**
     * 跳过订单数
     */
    private int skippedCount = 0;

    /**
     * 调度轮数
     */
    private int rounds = 0;

    /**
     * 剩余订单数
     */
    private long remainingCount = 0;

    /**
     * 剩余高优比例
     */
    private double remainingHighPriorityRatio = 0.0;
}
