package com.itheima.chaincloud.domain.base.truck;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 车辆
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 车辆类型id
     */
    private Long truckTypeId;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 车牌号码
     */
    private String licensePlate;

    /**
     * GPS设备id
     */
    private String deviceGpsId;

    /**
     * 准载重量
     */
    private BigDecimal allowableLoad;

    /**
     * 准载体积
     */
    private BigDecimal allowableVolume;

    /**
     * 车辆行驶证信息id
     */
    private Long truckLicenseId;

    /**
     * 状态 0：禁用 1：正常
     */
    private Integer status;

    /**
     * 运输状态 0：已到达 1：运输中
     */
    private Integer runStatus;

    /**
     * 工作状态
     */
    private Integer workStatus;

    /**
     * 所在机构id
     */
    private Long currentOrganId;

    /**
     * 图片信息
     */
    private String picture;

    /**
     * 满载系数
     */
    private Double loadingRatio;

    /**
     * 司机数量
     */
    @ApiModelProperty(value = "司机数量")
    private Integer driverNum;

    /**
     * 车辆类型名称
     */
    @ApiModelProperty(value = "车辆类型名称")
    private String truckTypeName;
}