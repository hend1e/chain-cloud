package com.itheima.chaincloud.domain.driver.domain;

import com.itheima.chaincloud.domain.base.enums.TruckAccidentTypeEnum;
import com.itheima.chaincloud.domain.base.enums.TruckBreakRulesTypeEnum;
import com.itheima.chaincloud.domain.base.enums.TruckFaultTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverReturnRegisterDTO {
    @ApiModelProperty(value = "运输任务id", required = true)
    private String id;

    @ApiModelProperty(value = "出车时间", required = true, example = "2022-07-18 17:00:00")
    private String outStorageTime;

    @ApiModelProperty(value = "回车时间", required = true, example = "2022-07-18 17:00:00")
    private String intoStorageTime;

    @ApiModelProperty(value = "车辆是否违章", required = true)
    private Boolean isBreakRules;

    @ApiModelProperty(value = "违章类型，1-闯红灯...6-其他")
    private TruckBreakRulesTypeEnum breakRulesType;

    @ApiModelProperty(value = "违章说明，类型为“其他”时填写")
    private String breakRulesDescription;

    @ApiModelProperty(value = "罚款金额")
    private BigDecimal penaltyAmount;

    @ApiModelProperty(value = "扣分数据")
    private Integer deductPoints;

    @ApiModelProperty(value = "车辆是否故障", required = true)
    private Boolean isFault;

    @ApiModelProperty(value = "车辆是否可用")
    private Boolean isAvailable;

    @ApiModelProperty(value = "故障类型，1-发动机启动困难...9-其他")
    private TruckFaultTypeEnum faultType;

    @ApiModelProperty(value = "故障说明，类型为“其他”时填写")
    private String faultDescription;

    @ApiModelProperty(value = "故障图片")
    private String faultImages;

    @ApiModelProperty(value = "是否出现事故", required = true)
    private Boolean isAccident;

    @ApiModelProperty(value = "事故类型，1-直行事故...9-其他")
    private TruckAccidentTypeEnum accidentType;

    @ApiModelProperty(value = "事故说明，类型为“其他”时填写")
    private String accidentDescription;

    @ApiModelProperty(value = "事故图片")
    private String accidentImages;
}
