package com.itheima.ms.work.mq.bean;

import lombok.Data;

import java.util.List;

/**
 * 运输任务创建消息DTO
 */
@Data
public class TransportTaskCreateMsgDTO {
    private List<Long> driverIds;
    private Long truckPlanId;
    private Long truckId;
    private Long startOrganId;
    private Long endOrganId;
    private Long transportTripsId;
    private Double totalVolume;
    private Double totalWeight;
    private List<String> transportOrderIdList;
    private String dispatchDecisionId;
}
