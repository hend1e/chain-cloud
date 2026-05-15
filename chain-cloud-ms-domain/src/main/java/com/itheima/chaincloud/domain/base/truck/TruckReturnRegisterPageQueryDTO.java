package com.itheima.chaincloud.domain.base.truck;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 回车登记分页查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckReturnRegisterPageQueryDTO {

    /**
     * 页码
     */
    private Integer page;

    /**
     * 页面大小
     */
    private Integer pageSize;

    /**
     * 运输任务id,用于模糊查询
     */
    private Long transportTaskId;

    /**
     * 运输任务id列表，用于精确查询
     */
    private List<Long> transportTaskIds;

    /**
     * 开始回车时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime intoStorageStartTime;

    /**
     * 结束回车时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime intoStorageEndTime;

    /**
     * 车辆是否可用，1-是，0-否
     */
    private Boolean isAvailable;
}