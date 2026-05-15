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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransportTaskServiceImpl extends
        ServiceImpl<TransportTaskMapper, TransportTaskEntity> implements TransportTaskService {

    @Resource
    private TransportOrderTaskService transportOrderTaskService;
    @Resource
    private DriverJobFeign driverJobFeign;
    @Resource
    private MQFeign mqFeign;

    @Override
    @Transactional
    public Boolean updateStatus(Long id, TransportTaskStatusEnum status) {
        if (TransportTaskStatusEnum.PENDING == status) throw new CCException(WorkExceptionEnum.TRANSPORT_TASK_STATUS_NOT_PENDING);
        if (TransportTaskStatusEnum.PROCESSING == status) {
            TransportTaskStartDTO transportTaskStartDTO = new TransportTaskStartDTO();
            transportTaskStartDTO.setTransportTaskId(String.valueOf(id));
            this.startTransportTask(transportTaskStartDTO);
            return true;
        } else if (TransportTaskStatusEnum.COMPLETED == status) {
            TransportTaskCompleteDTO transportTaskCompleteDTO = new TransportTaskCompleteDTO();
            transportTaskCompleteDTO.setTransportTaskId(String.valueOf(id));
            this.completeTransportTask(transportTaskCompleteDTO);
            return true;
        } else if (TransportTaskStatusEnum.CANCELLED == status) {
            List<String> transportOrderIdList = this.queryTransportOrderIdListById(id);
            if (CollUtil.isNotEmpty(transportOrderIdList)) throw new CCException(WorkExceptionEnum.TRANSPORT_TASK_CANCEL_FAILED);
            TransportTaskEntity transportTaskEntity = new TransportTaskEntity();
            transportTaskEntity.setId(id);
            transportTaskEntity.setStatus(status);
            this.updateById(transportTaskEntity);
            DriverJobPageQueryDTO driverJobPageQueryDTO = DriverJobPageQueryDTO.builder().page(1).pageSize(999).transportTaskId(id).build();
            PageResponse<DriverJobDTO> driverJobPage = driverJobFeign.pageQuery(driverJobPageQueryDTO);
            driverJobPage.getItems().forEach(dto -> driverJobFeign.updateStatus(dto.getId(), DriverJobStatusEnum.CANCELLED));
            return true;
        } else {
            TransportTaskEntity transportTaskEntity = new TransportTaskEntity();
            transportTaskEntity.setId(id);
            transportTaskEntity.setStatus(status);
            return super.updateById(transportTaskEntity);
        }
    }

    @Override
    public PageResponse<TransportTaskDTO> findByPage(TransportTaskPageQueryDTO pageQueryDTO) {
        Page<TransportTaskEntity> pageQuery = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        LambdaQueryWrapper<TransportTaskEntity> lambdaQueryWrapper = Wrappers.<TransportTaskEntity>lambdaQuery()
                .like(ObjectUtil.isNotEmpty(pageQueryDTO.getId()), TransportTaskEntity::getId, pageQueryDTO.getId())
                .eq(ObjectUtil.isNotEmpty(pageQueryDTO.getStatus()), TransportTaskEntity::getStatus, pageQueryDTO.getStatus())
                .eq(ObjectUtil.isNotEmpty(pageQueryDTO.getAssignedStatus()), TransportTaskEntity::getAssignedStatus, pageQueryDTO.getAssignedStatus())
                .eq(ObjectUtil.isNotEmpty(pageQueryDTO.getLoadingStatus()), TransportTaskEntity::getLoadingStatus, pageQueryDTO.getLoadingStatus())
                .eq(ObjectUtil.isNotEmpty(pageQueryDTO.getStartAgencyId()), TransportTaskEntity::getStartAgencyId, pageQueryDTO.getStartAgencyId())
                .eq(ObjectUtil.isNotEmpty(pageQueryDTO.getEndAgencyId()), TransportTaskEntity::getEndAgencyId, pageQueryDTO.getEndAgencyId())
                .in(ObjectUtil.isNotEmpty(pageQueryDTO.getTruckIds()), TransportTaskEntity::getTruckId, pageQueryDTO.getTruckIds())
                .eq(ObjectUtil.isNotEmpty(pageQueryDTO.getTruckId()), TransportTaskEntity::getTruckId, pageQueryDTO.getTruckId())
                .orderByDesc(TransportTaskEntity::getPlanDepartureTime);
        Page<TransportTaskEntity> pageResult = super.page(pageQuery, lambdaQueryWrapper);
        if (CollUtil.isEmpty(pageResult.getRecords())) return new PageResponse<>(pageResult);
        PageResponse<TransportTaskDTO> pageResponse = new PageResponse<>(pageResult, TransportTaskDTO.class);
        pageResponse.getItems().forEach(transportTask -> {
            List<TransportOrderTaskEntity> list = this.transportOrderTaskService.findAll(null, transportTask.getId());
            List<String> transportOrderIdList = CollUtil.getFieldValues(list, "transportOrderId", String.class);
            transportTask.setTransportOrderIds(transportOrderIdList);
            transportTask.setTransportOrderCount(CollUtil.size(transportOrderIdList));
        });
        return pageResponse;
    }

    @Override
    public List<TransportTaskEntity> findAll(List<Long> ids, Long id, Integer status, TransportTaskDTO dto) {
        LambdaQueryWrapper<TransportTaskEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(CollUtil.isNotEmpty(ids), TransportTaskEntity::getId, ids);
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(id), TransportTaskEntity::getId, id);
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(status), TransportTaskEntity::getStatus, status);
        if (ObjectUtil.isNotEmpty(dto)) lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(dto.getTruckId()), TransportTaskEntity::getTruckId, dto.getTruckId());
        lambdaQueryWrapper.orderByDesc(TransportTaskEntity::getCreated);
        return list(lambdaQueryWrapper);
    }

    @Override
    public void completeTransportTask(TransportTaskCompleteDTO transportTaskCompleteDTO) {
        TransportTaskEntity transportTaskEntity = new TransportTaskEntity();
        transportTaskEntity.setId(Long.valueOf(transportTaskCompleteDTO.getTransportTaskId()));
        transportTaskEntity.setStatus(TransportTaskStatusEnum.COMPLETED);
        transportTaskEntity.setTransportCertificate(transportTaskCompleteDTO.getTransportCertificate());
        transportTaskEntity.setDeliverPicture(transportTaskCompleteDTO.getDeliverPicture());
        transportTaskEntity.setActualArrivalTime(LocalDateTime.now());
        try {
            var transportTask = super.getById(transportTaskCompleteDTO.getTransportTaskId());
            var mark = transportTask.getMark();
            if (StrUtil.isNotEmpty(mark)) {
                var jsonObj = JSONUtil.parseObj(mark);
                var dispatchDecisionId = jsonObj.getStr("dispatchDecisionId");
                if (StrUtil.isNotEmpty(dispatchDecisionId)) {
                    boolean isDelayed = LocalDateTime.now().isAfter(transportTask.getPlanArrivalTime());
                    var msg = JSONUtil.toJsonStr(Map.of("dispatchDecisionId", dispatchDecisionId, "isDelayed", isDelayed, "manualAdjusted", false, "date", System.currentTimeMillis()));
                    this.mqFeign.sendMsg(Constants.MQ.Exchanges.AIGC, Constants.MQ.RoutingKeys.COMPLETE, msg);
                }
            }
        } catch (Exception e) { log.warn("发送消息失败", e); }
        super.updateById(transportTaskEntity);
    }

    @Override
    public void startTransportTask(TransportTaskStartDTO transportTaskStartDTO) {
        TransportTaskEntity transportTaskEntity = new TransportTaskEntity();
        transportTaskEntity.setId(Long.valueOf(transportTaskStartDTO.getTransportTaskId()));
        transportTaskEntity.setStatus(TransportTaskStatusEnum.PROCESSING);
        transportTaskEntity.setCargoPickUpPicture(transportTaskStartDTO.getCargoPickUpPicture());
        transportTaskEntity.setCargoPicture(transportTaskStartDTO.getCargoPicture());
        transportTaskEntity.setActualDepartureTime(LocalDateTime.now());
        super.updateById(transportTaskEntity);
    }

    @Override
    public List<String> queryTransportOrderIdListById(Long id) {
        LambdaQueryWrapper<TransportOrderTaskEntity> queryWrapper = new LambdaQueryWrapper<TransportOrderTaskEntity>()
                .eq(TransportOrderTaskEntity::getTransportTaskId, id);
        List<TransportOrderTaskEntity> orderList = this.transportOrderTaskService.list(queryWrapper);
        if (CollUtil.isEmpty(orderList)) return Collections.emptyList();
        return orderList.stream().map(TransportOrderTaskEntity::getTransportOrderId).collect(Collectors.toList());
    }

    @Override
    public List<TransportTaskDTO> findAllByOrderIdOrTaskId(String transportOrderId, Long transportTaskId) {
        if (ObjectUtil.isAllEmpty(transportOrderId, transportTaskId)) throw new CCException(WorkExceptionEnum.TRANSPORT_TASK_QUERY_PARAM_ERROR);
        if (ObjectUtil.isNotEmpty(transportTaskId)) {
            TransportTaskEntity transportTask = super.getById(transportTaskId);
            if (ObjectUtil.isEmpty(transportTask)) throw new CCException(WorkExceptionEnum.TRANSPORT_TASK_NOT_FOUND);
            return ListUtil.toList(BeanUtil.toBean(transportTask, TransportTaskDTO.class));
        }
        List<TransportOrderTaskEntity> transportOrderTaskList = transportOrderTaskService.findAll(transportOrderId, null);
        if (CollUtil.isEmpty(transportOrderTaskList)) return ListUtil.empty();
        List<Long> transportTaskIds = CollUtil.getFieldValues(transportOrderTaskList, "transportTaskId", Long.class);
        List<TransportTaskEntity> transportTasList = super.listByIds(transportTaskIds);
        return BeanUtil.copyToList(transportTasList, TransportTaskDTO.class);
    }

    @Override
    public TransportTaskDTO findById(Long id) {
        TransportTaskEntity transportTask = super.getById(id);
        if (ObjectUtil.isEmpty(transportTask)) throw new CCException(WorkExceptionEnum.TRANSPORT_TASK_NOT_FOUND);
        TransportTaskDTO transportTaskDTO = BeanUtil.toBean(transportTask, TransportTaskDTO.class);
        List<TransportOrderTaskEntity> list = this.transportOrderTaskService.findAll(null, transportTask.getId());
        List<String> transportOrderIdList = CollUtil.getFieldValues(list, "transportOrderId", String.class);
        transportTaskDTO.setTransportOrderIds(transportOrderIdList);
        transportTaskDTO.setTransportOrderCount(CollUtil.size(transportOrderIdList));
        return transportTaskDTO;
    }

    @Override
    public List<TransportTaskStatusCountDTO> groupByStatus() {
        List<TransportTaskStatusCountDTO> statusCountList = Arrays.stream(TransportTaskStatusEnum.values())
                .map(status -> TransportTaskStatusCountDTO.builder().status(status).statusCode(status.getCode()).count(0L).build())
                .collect(Collectors.toList());
        List<TransportTaskStatusCountDTO> statusCount = super.baseMapper.findStatusCount();
        for (TransportTaskStatusCountDTO statusCountDTO : statusCountList) {
            for (TransportTaskStatusCountDTO countDTO : statusCount) {
                if (ObjectUtil.equal(statusCountDTO.getStatusCode(), countDTO.getStatusCode())) {
                    statusCountDTO.setCount(countDTO.getCount()); break;
                }
            }
        }
        return statusCountList;
    }

    @Transactional
    @Override
    public void adjust(TaskTransportUpdateDTO dto) {
        if (ObjectUtil.hasEmpty(dto, dto.getId()) || ObjectUtil.isAllEmpty(dto.getTransportTripsId(), dto.getTruckId(), dto.getDriverIds()))
            throw new CCException(WorkExceptionEnum.TRANSPORT_TASK_UPDATE_PARAM_ERROR);
        TransportTaskEntity transportTaskEntity = BeanUtil.toBean(dto, TransportTaskEntity.class);
        super.updateById(transportTaskEntity);
        this.driverJobFeign.removeByTransportTaskId(dto.getId());
        dto.getDriverIds().forEach(driverId -> this.driverJobFeign.createDriverJob(dto.getId(), driverId));
    }

    @Override
    public void delayedDelivery(TransportTaskDelayDeliveryDTO transportTaskDelayDeliveryDTO) {
        LocalDateTime delayTime = LocalDateTimeUtil.parse(transportTaskDelayDeliveryDTO.getDelayTime(), DatePattern.NORM_DATETIME_PATTERN);
        LambdaUpdateWrapper<TransportTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ObjectUtil.isNotEmpty(transportTaskDelayDeliveryDTO.getTransportTaskId()), TransportTaskEntity::getId, transportTaskDelayDeliveryDTO.getTransportTaskId())
                .set(ObjectUtil.isNotEmpty(delayTime), TransportTaskEntity::getPlanDepartureTime, delayTime)
                .set(ObjectUtil.isNotEmpty(transportTaskDelayDeliveryDTO.getDelayReason()), TransportTaskEntity::getMark, transportTaskDelayDeliveryDTO.getDelayReason());
        this.update(updateWrapper);
    }

    @Override
    public List<TransportTaskMonthlyDistanceDTO> monthlyDistanceStatistics(List<String> transportTaskIds, String month) {
        DateTime dateTime = DateUtil.parse(month, DatePattern.NORM_MONTH_PATTERN);
        LocalDateTime startTime = LocalDateTimeUtil.of(dateTime);
        LocalDateTime endTime = LocalDateTimeUtil.of(DateUtil.endOfMonth(dateTime));
        return this.baseMapper.monthlyDistanceStatistics(transportTaskIds, startTime, endTime);
    }

    @Override
    public List<Long> findByAgencyId(Long startAgencyId, Long endAgencyId) {
        LambdaQueryWrapper<TransportTaskEntity> queryWrapper = Wrappers.<TransportTaskEntity>lambdaQuery()
                .eq(ObjectUtil.isNotEmpty(startAgencyId), TransportTaskEntity::getStartAgencyId, startAgencyId)
                .eq(ObjectUtil.isNotEmpty(endAgencyId), TransportTaskEntity::getEndAgencyId, endAgencyId);
        List<TransportTaskEntity> transportTaskEntityList = this.list(queryWrapper);
        return transportTaskEntityList.stream().map(TransportTaskEntity::getId).collect(Collectors.toList());
    }

    @Override
    public Long countByTruckId(Long truckId) {
        return count(Wrappers.<TransportTaskEntity>lambdaQuery()
                .in(TransportTaskEntity::getStatus, TransportTaskStatusEnum.PENDING.getCode(), TransportTaskStatusEnum.PROCESSING.getCode())
                .eq(TransportTaskEntity::getTruckId, truckId));
    }
}
