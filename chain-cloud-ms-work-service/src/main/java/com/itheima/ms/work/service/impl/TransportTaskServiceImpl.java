package com.itheima.ms.work.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.chaincloud.api.base.common.MQFeign;
import com.itheima.chaincloud.api.driver.DriverJobFeign;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.common.util.StrUtil;
import com.itheima.chaincloud.domain.driver.domain.*;
import com.itheima.chaincloud.domain.driver.enums.DriverJobStatusEnum;
import com.itheima.chaincloud.domain.work.domain.*;
import com.itheima.chaincloud.domain.work.domain.request.*;
import com.itheima.chaincloud.domain.work.domain.response.*;
import com.itheima.chaincloud.domain.work.enums.*;
import com.itheima.ms.work.entity.TransportOrderTaskEntity;
import com.itheima.ms.work.entity.TransportTaskEntity;
import com.itheima.ms.work.mapper.TransportTaskMapper;
import com.itheima.ms.work.service.TransportOrderTaskService;
import com.itheima.ms.work.service.TransportTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransportTaskServiceImpl extends
        ServiceImpl<TransportTaskMapper, TransportTaskEntity> implements TransportTaskService {

    private final TransportOrderTaskService transportOrderTaskService;
    private final DriverJobFeign driverJobFeign;
    private final MQFeign mqFeign;

    @Override
    @Transactional
    public Boolean updateStatus(Long id, TransportTaskStatusEnum status) {
        if (TransportTaskStatusEnum.PENDING == status) {
            //修改运输任务状态不能为待执行状态
            throw new CCException(WorkExceptionEnum.TRANSPORT_TASK_STATUS_NOT_PENDING);
        }
        if (TransportTaskStatusEnum.PROCESSING == status) {
            //开始运输任务
            var transportTaskStartDTO = new TransportTaskStartDTO();
            transportTaskStartDTO.setTransportTaskId(String.valueOf(id));
            this.startTransportTask(transportTaskStartDTO);
            return true;
        } else if (TransportTaskStatusEnum.COMPLETED == status) {
            //完成运输任务
            var transportTaskCompleteDTO = new TransportTaskCompleteDTO();
            transportTaskCompleteDTO.setTransportTaskId(String.valueOf(id));
            this.completeTransportTask(transportTaskCompleteDTO);
            return true;
        } else if (TransportTaskStatusEnum.CANCELLED == status) {
            //取消运输任务，需校验是否有未完成的运单
            var transportOrderIdList = this.queryTransportOrderIdListById(id);
            if (CollUtil.isNotEmpty(transportOrderIdList)) {
                throw new CCException(WorkExceptionEnum.TRANSPORT_TASK_CANCEL_FAILED);
            }
            var transportTaskEntity = new TransportTaskEntity();
            transportTaskEntity.setId(id);
            transportTaskEntity.setStatus(status);
            super.updateById(transportTaskEntity);
            //查询并取消关联的司机作业单
            var driverJobPageQueryDTO = DriverJobPageQueryDTO.builder()
                    .page(1)
                    .pageSize(999)
                    .transportTaskId(id)
                    .build();
            var driverJobPage = this.driverJobFeign.pageQuery(driverJobPageQueryDTO);
            driverJobPage.getItems().forEach(dto -> this.driverJobFeign.updateStatus(dto.getId(), DriverJobStatusEnum.CANCELLED));
            return true;
        } else {
            //修改其他状态
            var transportTaskEntity = new TransportTaskEntity();
            transportTaskEntity.setId(id);
            transportTaskEntity.setStatus(status);
            return super.updateById(transportTaskEntity);
        }
    }

    @Override
    public PageResponse<TransportTaskDTO> findByPage(TransportTaskPageQueryDTO pageQueryDTO) {
        //查询运输任务
        var pageQuery = new Page<TransportTaskEntity>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        var lambdaQueryWrapper = Wrappers.<TransportTaskEntity>lambdaQuery()
                .like(ObjectUtil.isNotEmpty(pageQueryDTO.getId()), TransportTaskEntity::getId, pageQueryDTO.getId())
                .eq(ObjectUtil.isNotEmpty(pageQueryDTO.getStatus()), TransportTaskEntity::getStatus, pageQueryDTO.getStatus())
                .eq(ObjectUtil.isNotEmpty(pageQueryDTO.getAssignedStatus()), TransportTaskEntity::getAssignedStatus, pageQueryDTO.getAssignedStatus())
                .eq(ObjectUtil.isNotEmpty(pageQueryDTO.getLoadingStatus()), TransportTaskEntity::getLoadingStatus, pageQueryDTO.getLoadingStatus())
                .eq(ObjectUtil.isNotEmpty(pageQueryDTO.getStartAgencyId()), TransportTaskEntity::getStartAgencyId, pageQueryDTO.getStartAgencyId())
                .eq(ObjectUtil.isNotEmpty(pageQueryDTO.getEndAgencyId()), TransportTaskEntity::getEndAgencyId, pageQueryDTO.getEndAgencyId())
                .in(ObjectUtil.isNotEmpty(pageQueryDTO.getTruckIds()), TransportTaskEntity::getTruckId, pageQueryDTO.getTruckIds())
                .eq(ObjectUtil.isNotEmpty(pageQueryDTO.getTruckId()), TransportTaskEntity::getTruckId, pageQueryDTO.getTruckId())
                .orderByDesc(TransportTaskEntity::getPlanDepartureTime);
        var pageResult = super.page(pageQuery, lambdaQueryWrapper);
        if (CollUtil.isEmpty(pageResult.getRecords())) {
            return new PageResponse<>(pageResult);
        }
        var pageResponse = new PageResponse<>(pageResult, TransportTaskDTO.class);
        //填充每个运输任务的运单id列表
        pageResponse.getItems().forEach(transportTask -> {
            var list = this.transportOrderTaskService.findAll(null, transportTask.getId());
            var transportOrderIdList = CollUtil.getFieldValues(list, "transportOrderId", String.class);
            transportTask.setTransportOrderIds(transportOrderIdList);
            transportTask.setTransportOrderCount(CollUtil.size(transportOrderIdList));
        });
        return pageResponse;
    }

    @Override
    public List<TransportTaskEntity> findAll(List<Long> ids, Long id, Integer status, TransportTaskDTO dto) {
        var lambdaQueryWrapper = new LambdaQueryWrapper<TransportTaskEntity>();
        lambdaQueryWrapper.in(CollUtil.isNotEmpty(ids), TransportTaskEntity::getId, ids);
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(id), TransportTaskEntity::getId, id);
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(status), TransportTaskEntity::getStatus, status);
        if (ObjectUtil.isNotEmpty(dto)) {
            lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(dto.getTruckId()), TransportTaskEntity::getTruckId, dto.getTruckId());
        }
        lambdaQueryWrapper.orderByDesc(TransportTaskEntity::getCreated);
        return super.list(lambdaQueryWrapper);
    }

    /**
     * 完成运输任务
     */
    @Override
    public void completeTransportTask(TransportTaskCompleteDTO transportTaskCompleteDTO) {
        var transportTaskEntity = new TransportTaskEntity();
        transportTaskEntity.setId(Long.valueOf(transportTaskCompleteDTO.getTransportTaskId()));
        //已完成
        transportTaskEntity.setStatus(TransportTaskStatusEnum.COMPLETED);
        transportTaskEntity.setTransportCertificate(transportTaskCompleteDTO.getTransportCertificate());
        transportTaskEntity.setDeliverPicture(transportTaskCompleteDTO.getDeliverPicture());
        //实际到达时间
        transportTaskEntity.setActualArrivalTime(LocalDateTime.now());

        super.updateById(transportTaskEntity);
    }

    /**
     * 开始运输任务
     */
    @Override
    public void startTransportTask(TransportTaskStartDTO transportTaskStartDTO) {
        var transportTaskEntity = new TransportTaskEntity();
        transportTaskEntity.setId(Long.valueOf(transportTaskStartDTO.getTransportTaskId()));
        //进行中
        transportTaskEntity.setStatus(TransportTaskStatusEnum.PROCESSING);
        transportTaskEntity.setCargoPickUpPicture(transportTaskStartDTO.getCargoPickUpPicture());
        transportTaskEntity.setCargoPicture(transportTaskStartDTO.getCargoPicture());
        //实际发车时间
        transportTaskEntity.setActualDepartureTime(LocalDateTime.now());
        super.updateById(transportTaskEntity);
    }

    @Override
    public List<String> queryTransportOrderIdListById(Long id) {
        //通过运输任务找到运单id列表
        var queryWrapper = new LambdaQueryWrapper<TransportOrderTaskEntity>()
                .eq(TransportOrderTaskEntity::getTransportTaskId, id);
        var orderList = this.transportOrderTaskService.list(queryWrapper);
        if (CollUtil.isEmpty(orderList)) {
            return Collections.emptyList();
        }
        //运单id列表
        return orderList.stream()
                .map(TransportOrderTaskEntity::getTransportOrderId)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransportTaskDTO> findAllByOrderIdOrTaskId(String transportOrderId, Long transportTaskId) {
        if (ObjectUtil.isAllEmpty(transportOrderId, transportTaskId)) {
            throw new CCException(WorkExceptionEnum.TRANSPORT_TASK_QUERY_PARAM_ERROR);
        }
        if (ObjectUtil.isNotEmpty(transportTaskId)) {
            //按运输任务id查询
            var transportTask = super.getById(transportTaskId);
            if (ObjectUtil.isEmpty(transportTask)) {
                throw new CCException(WorkExceptionEnum.TRANSPORT_TASK_NOT_FOUND);
            }
            return ListUtil.toList(BeanUtil.toBean(transportTask, TransportTaskDTO.class));
        }
        //通过关联表查询出运输任务id列表
        var transportOrderTaskList = this.transportOrderTaskService.findAll(transportOrderId, null);
        if (CollUtil.isEmpty(transportOrderTaskList)) {
            return ListUtil.empty();
        }
        //根据运输任务id列表查询运输任务数据列表
        var transportTaskIds = CollUtil.getFieldValues(transportOrderTaskList, "transportTaskId", Long.class);
        var transportTasList = super.listByIds(transportTaskIds);
        return BeanUtil.copyToList(transportTasList, TransportTaskDTO.class);
    }

    @Override
    public TransportTaskDTO findById(Long id) {
        var transportTask = super.getById(id);
        if (ObjectUtil.isEmpty(transportTask)) {
            throw new CCException(WorkExceptionEnum.TRANSPORT_TASK_NOT_FOUND);
        }
        var transportTaskDTO = BeanUtil.toBean(transportTask, TransportTaskDTO.class);
        var list = this.transportOrderTaskService.findAll(null, transportTask.getId());
        var transportOrderIdList = CollUtil.getFieldValues(list, "transportOrderId", String.class);
        transportTaskDTO.setTransportOrderIds(transportOrderIdList);
        transportTaskDTO.setTransportOrderCount(CollUtil.size(transportOrderIdList));
        return transportTaskDTO;
    }

    @Override
    public List<TransportTaskStatusCountDTO> groupByStatus() {
        //将所有的枚举状态放到集合中
        var statusCountList = Arrays.stream(TransportTaskStatusEnum.values())
                .map(status -> TransportTaskStatusCountDTO.builder()
                        .status(status)
                        .statusCode(status.getCode())
                        .count(0L)
                        .build())
                .collect(Collectors.toList());
        //数据库根据状态统计
        var statusCount = super.baseMapper.findStatusCount();
        var statusCountMap = statusCount.stream()
                .collect(Collectors.toMap(TransportTaskStatusCountDTO::getStatusCode, TransportTaskStatusCountDTO::getCount));
        //将数量值放入到集合中，如果没有的数量默认值为0
        for (var statusCountDTO : statusCountList) {
            if (statusCountMap.containsKey(statusCountDTO.getStatusCode())) {
                statusCountDTO.setCount(statusCountMap.get(statusCountDTO.getStatusCode()));
            }
        }
        return statusCountList;
    }

    @Transactional
    @Override
    public void adjust(TaskTransportUpdateDTO dto) {
        if (ObjectUtil.hasEmpty(dto, dto.getId()) || ObjectUtil.isAllEmpty(dto.getTransportTripsId(), dto.getTruckId(), dto.getDriverIds())) {
            throw new CCException(WorkExceptionEnum.TRANSPORT_TASK_UPDATE_PARAM_ERROR);
        }
        //修改任务
        var transportTaskEntity = BeanUtil.toBean(dto, TransportTaskEntity.class);
        transportTaskEntity.setAssignedStatus(TransportTaskAssignedStatusEnum.DISTRIBUTED);
        super.updateById(transportTaskEntity);
        //删除之前对应的司机作业单
        this.driverJobFeign.removeByTransportTaskId(dto.getId());
        //创建新的司机作业单
        dto.getDriverIds().forEach(driverId -> this.driverJobFeign.createDriverJob(dto.getId(), driverId));
    }

    /**
     * 延迟提货
     */
    @Override
    public void delayedDelivery(TransportTaskDelayDeliveryDTO transportTaskDelayDeliveryDTO) {
        //将时间字符串转换格式
        var delayTime = LocalDateTimeUtil.parse(transportTaskDelayDeliveryDTO.getDelayTime(), DatePattern.NORM_DATETIME_PATTERN);
        //构建更新条件，根据任务id更新计划出发时间和备注
        var updateWrapper = new LambdaUpdateWrapper<TransportTaskEntity>();
        updateWrapper.eq(ObjectUtil.isNotEmpty(transportTaskDelayDeliveryDTO.getTransportTaskId()), TransportTaskEntity::getId, transportTaskDelayDeliveryDTO.getTransportTaskId())
                .set(ObjectUtil.isNotEmpty(delayTime), TransportTaskEntity::getPlanDepartureTime, delayTime)
                .set(ObjectUtil.isNotEmpty(transportTaskDelayDeliveryDTO.getDelayReason()), TransportTaskEntity::getMark, transportTaskDelayDeliveryDTO.getDelayReason());
        //更新运输任务
        super.update(updateWrapper);
    }

    /**
     * 任务里程统计
     */
    @Override
    public List<TransportTaskMonthlyDistanceDTO> monthlyDistanceStatistics(List<String> transportTaskIds, String month) {
        //月度起止时间
        var dateTime = DateUtil.parse(month, DatePattern.NORM_MONTH_PATTERN);
        var startTime = LocalDateTimeUtil.of(dateTime);
        var endTime = LocalDateTimeUtil.of(DateUtil.endOfMonth(dateTime));
        //根据任务id列表查询
        return this.baseMapper.monthlyDistanceStatistics(transportTaskIds, startTime, endTime);
    }

    /**
     * 根据起始机构查询运输任务id列表
     */
    @Override
    public List<Long> findByAgencyId(Long startAgencyId, Long endAgencyId) {
        //构造查询条件
        var queryWrapper = Wrappers.<TransportTaskEntity>lambdaQuery()
                .eq(ObjectUtil.isNotEmpty(startAgencyId), TransportTaskEntity::getStartAgencyId, startAgencyId)
                .eq(ObjectUtil.isNotEmpty(endAgencyId), TransportTaskEntity::getEndAgencyId, endAgencyId);
        //根据起始机构查询运输任务
        var transportTaskEntityList = super.list(queryWrapper);
        //从运输任务中抽取id
        return transportTaskEntityList.stream()
                .map(TransportTaskEntity::getId)
                .collect(Collectors.toList());
    }

    /**
     * 根据车辆ID和状态统计
     */
    @Override
    public Long countByTruckId(Long truckId) {
        return super.count(Wrappers.<TransportTaskEntity>lambdaQuery()
                .in(TransportTaskEntity::getStatus, TransportTaskStatusEnum.PENDING.getCode(), TransportTaskStatusEnum.PROCESSING.getCode())
                .eq(TransportTaskEntity::getTruckId, truckId));
    }
}
