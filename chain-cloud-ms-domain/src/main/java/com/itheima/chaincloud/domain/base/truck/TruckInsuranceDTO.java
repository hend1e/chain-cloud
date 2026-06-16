package com.itheima.chaincloud.domain.base.truck;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 车辆保险
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckInsuranceDTO {

    /**
     * id
     */
    private String id;

    /**
     * 车辆id
     */
    private String truckId;

    /**
     * 保险类型，1为交强险
     */
    private Integer type;

    /**
     * 车辆排放标准
     */
    private String emissionsStandard;

    /**
     * 三责有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate threeLiabilityExpiration;

    /**
     * 被保险人
     */
    private String insured;

    /**
     * 三责险
     */
    private String threeLiability;

    /**
     * 交强险有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate compulsoryInsuranceExpiration;

    /**
     * 交强险
     */
    private String compulsoryInsurance;
}