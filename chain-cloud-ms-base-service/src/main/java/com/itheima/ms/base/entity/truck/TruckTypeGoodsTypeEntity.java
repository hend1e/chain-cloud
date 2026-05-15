package com.itheima.ms.base.entity.truck;

import com.baomidou.mybatisplus.annotation.TableName;
import com.itheima.chaincloud.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车辆类型与货物类型关联表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cc_truck_type_goods_type")
public class TruckTypeGoodsTypeEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 车辆类型id
     */
    private Long truckTypeId;

    /**
     * 货物类型id
     */
    private Long goodsTypeId;

}
