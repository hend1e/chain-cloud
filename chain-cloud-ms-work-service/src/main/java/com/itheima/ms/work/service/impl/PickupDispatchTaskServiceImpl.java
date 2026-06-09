package com.itheima.ms.work.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.chaincloud.api.base.common.AreaFeign;
import com.itheima.chaincloud.api.base.common.AuthFeign;
import com.itheima.chaincloud.api.base.common.MQFeign;
import com.itheima.chaincloud.api.base.common.MessageFeign;
import com.itheima.chaincloud.api.oms.OrderFeign;
import com.itheima.chaincloud.api.search.CourierTaskFeign;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.BeanUtil;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.common.util.StrUtil;
import com.itheima.chaincloud.common.vo.OrderMsg;
import com.itheima.chaincloud.common.vo.TransportInfoMsg;
import com.itheima.chaincloud.domain.base.basic.MessageAddDTO;
import com.itheima.chaincloud.domain.base.constants.MessageConstants;
import com.itheima.chaincloud.domain.base.enums.MessageBussinessTypeEnum;
import com.itheima.chaincloud.domain.base.enums.MessageContentTypeEnum;
import com.itheima.chaincloud.domain.oms.domain.OrderDTO;
import com.itheima.chaincloud.domain.work.domain.*;
import com.itheima.chaincloud.domain.work.domain.request.PickupDispatchTaskPageQueryDTO;
import com.itheima.chaincloud.domain.work.domain.response.PickupDispatchTaskStatisticsDTO;
import com.itheima.chaincloud.domain.oms.enums.OrderStatusEnum;
import com.itheima.chaincloud.domain.search.domain.CourierTaskDTO;
import com.itheima.chaincloud.domain.work.enums.*;
import com.itheima.ms.work.entity.PickupDispatchTaskEntity;
import com.itheima.ms.work.mapper.TaskPickupDispatchMapper;
import com.itheima.ms.work.service.PickupDispatchTaskService;
import com.itheima.ms.work.service.TransportOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PickupDispatchTaskServiceImpl extends ServiceImpl<TaskPickupDispatchMapper, PickupDispatchTaskEntity> implements PickupDispatchTaskService {

    private final OrderFeign orderFeign;
    private final TaskPickupDispatchMapper taskPickupDispatchMapper;
    private final CourierTaskFeign courierTaskFeign;
    private final MQFeign mqFeign;
    private final TransportOrderService transportOrderService;
    private final AreaFeign areaFeign;
    private final AuthFeign authFeign;
    private final MessageFeign messageFeign;

    // 更新取派件任务状态
    @Override
    @Transactional
    public Boolean updateStatus(PickupDispatchTaskDTO pickupDispatchTaskDTO) {
        var paramError = WorkExceptionEnum.PICKUP_DISPATCH_TASK_PARAM_ERROR;
        if (ObjectUtil.hasEmpty(pickupDispatchTaskDTO.getId(), pickupDispatchTaskDTO.getStatus())) {
            throw new CCException("更新取派件任务状态，id或status不能为空", paramError.getCode());
        }
        var pickupDispatchTask = super.getById(pickupDispatchTaskDTO.getId());
        switch (pickupDispatchTaskDTO.getStatus()) {
            case NEW: {
                throw new CCException(WorkExceptionEnum.PICKUP_DISPATCH_TASK_STATUS_NOT_NEW);
            }
            case COMPLETED: {
                // 派件完成需校验签收信息
                pickupDispatchTask.setStatus(PickupDispatchTaskStatusEnum.COMPLETED);
                pickupDispatchTask.setActualEndTime(LocalDateTime.now());
                if (PickupDispatchTaskTypeEnum.DISPATCH == pickupDispatchTask.getTaskType()) {
                    if (ObjectUtil.isEmpty(pickupDispatchTaskDTO.getSignStatus())) {
                        throw new CCException("完成派件任务，签收状态不能为空", paramError.getCode());
                    }
                    pickupDispatchTask.setSignStatus(pickupDispatchTaskDTO.getSignStatus());
                    if (PickupDispatchTaskSignStatusEnum.RECEIVED == pickupDispatchTaskDTO.getSignStatus()) {
                        if (ObjectUtil.isEmpty(pickupDispatchTaskDTO.getSignRecipient())) {
                            throw new CCException("完成派件任务，签收人不能为空", paramError.getCode());
                        }
                        pickupDispatchTask.setSignRecipient(pickupDispatchTaskDTO.getSignRecipient());
                    }
                }
                break;
            }
            case CANCELLED: {
                // 取消任务：回退网点/用户取消/其他关闭
                if (ObjectUtil.isEmpty(pickupDispatchTaskDTO.getCancelReason())) {
                    throw new CCException("取消任务，原因不能为空", paramError.getCode());
                }
                pickupDispatchTask.setStatus(PickupDispatchTaskStatusEnum.CANCELLED);
                pickupDispatchTask.setCancelReason(pickupDispatchTaskDTO.getCancelReason());
                pickupDispatchTask.setCancelReasonDescription(pickupDispatchTaskDTO.getCancelReasonDescription());
                pickupDispatchTask.setCancelTime(LocalDateTime.now());
                if (pickupDispatchTaskDTO.getCancelReason() == PickupDispatchTaskCancelReasonEnum.RETURN_TO_AGENCY) {
                    var orderMsg = OrderMsg.builder()
                            .agencyId(pickupDispatchTask.getAgencyId())
                            .orderId(pickupDispatchTask.getOrderId())
                            .created(DateUtil.current())
                            .taskType(PickupDispatchTaskTypeEnum.PICKUP.getCode())
                            .mark(pickupDispatchTask.getMark())
                            .estimatedEndTime(pickupDispatchTask.getEstimatedEndTime())
                            .build();
                    //发送消息（取消任务发生在取件之前，没有运单，参数直接填入null）
                    this.transportOrderService.sendPickupDispatchTaskMsgToDispatch(null, orderMsg);
                } else if (pickupDispatchTaskDTO.getCancelReason() == PickupDispatchTaskCancelReasonEnum.CANCEL_BY_USER) {
                    this.orderFeign.updateStatus(ListUtil.of(pickupDispatchTask.getOrderId()), OrderStatusEnum.CANCELLED.getCode());
                } else {
                    this.orderFeign.updateStatus(ListUtil.of(pickupDispatchTask.getOrderId()), OrderStatusEnum.CLOSE.getCode());
                }
                break;
            }
            default: {
                throw new CCException("其他未知状态，不能完成更新操作", paramError.getCode());
            }
        }
        return super.updateById(pickupDispatchTask);
    }

    // 更新快递员：校验原快递员一致性
    @Override
    @Transactional
    public Boolean updateCourierId(List<Long> ids, Long originalCourierId, Long targetCourierId) {
        if (ObjectUtil.hasEmpty(ids, targetCourierId, originalCourierId)) {
            throw new CCException(WorkExceptionEnum.UPDATE_COURIER_PARAM_ERROR);
        }
        if (ObjectUtil.equal(originalCourierId, targetCourierId)) {
            throw new CCException(WorkExceptionEnum.UPDATE_COURIER_EQUAL_PARAM_ERROR);
        }
        var entities = super.listByIds(ids);
        if (CollUtil.isEmpty(entities)) {
            throw new CCException(WorkExceptionEnum.PICKUP_DISPATCH_TASK_NOT_FOUND);
        }
        var taskEntityList = StreamUtil.of(entities)
                .filter(entity -> ObjectUtil.isNotEmpty(entity.getCourierId()))
                .filter(entity -> ObjectUtil.notEqual(entity.getCourierId(), originalCourierId))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(taskEntityList)) {
            throw new CCException(WorkExceptionEnum.UPDATE_COURIER_ID_PARAM_ERROR);
        }
        var taskIds = CollStreamUtil.toList(entities, PickupDispatchTaskEntity::getId);
        var updateWrapper = Wrappers.<PickupDispatchTaskEntity>lambdaUpdate()
                .in(PickupDispatchTaskEntity::getId, taskIds)
                .set(PickupDispatchTaskEntity::getCourierId, targetCourierId)
                .set(PickupDispatchTaskEntity::getAssignedStatus, PickupDispatchTaskAssignedStatusEnum.DISTRIBUTED);
        return super.update(updateWrapper);
    }

    @Override
    @Transactional
    public PickupDispatchTaskEntity saveTaskPickupDispatch(PickupDispatchTaskEntity taskPickupDispatch) {
        taskPickupDispatch.setStatus(PickupDispatchTaskStatusEnum.NEW);
        var result = super.save(taskPickupDispatch);
        if (result) {
            //同步快递员任务到es
            this.syncCourierTask2Es(taskPickupDispatch);

            //生成运单跟踪消息和快递员端取件/派件消息通知
            this.generateMsg(taskPickupDispatch);

            return taskPickupDispatch;
        }
        throw new CCException(WorkExceptionEnum.PICKUP_DISPATCH_TASK_SAVE_ERROR);
    }

    /**
     * 生成运单跟踪消息和快递员端取件/派件消息通知
     *
     * @param taskPickupDispatch 取派件任务
     */
    private void generateMsg(PickupDispatchTaskEntity taskPickupDispatch) {
        var orderDTO = orderFeign.findById(taskPickupDispatch.getOrderId());
        //分配给快递员的取派件任务需要发送消息
        if (ObjectUtil.isNotEmpty(taskPickupDispatch.getCourierId())) {
            String title;
            String content;
            Integer contentType;
            String address;
            if (ObjectUtil.equal(taskPickupDispatch.getTaskType(), PickupDispatchTaskTypeEnum.DISPATCH)) {
                //派件任务需要发送运单跟踪消息
                this.sendTransportInfoMsg(taskPickupDispatch);

                //派件任务需要生成派件相关通知消息
                //组装消息内容
                address = areaFeign.get(orderDTO.getReceiverProvinceId()).getName()
                        + areaFeign.get(orderDTO.getReceiverCityId()).getName()
                        + areaFeign.get(orderDTO.getReceiverCountyId()).getName()
                        + orderDTO.getSenderAddress();
                content = StrUtil.format("地址：{}，收件人：{}，电话：{}", address, orderDTO.getReceiverName(), orderDTO.getReceiverPhone());

                title = MessageConstants.MessageTitle.DISPATCH;
                contentType = MessageContentTypeEnum.COURIER_DISPATCH.getCode();
            } else {
                //取件任务需要生成取件相关通知消息
                //组装消息内容
                address = areaFeign.get(orderDTO.getSenderProvinceId()).getName()
                        + areaFeign.get(orderDTO.getSenderCityId()).getName()
                        + areaFeign.get(orderDTO.getSenderCountyId()).getName()
                        + orderDTO.getSenderAddress();
                content = StrUtil.format("地址：{}，寄件人：{}，电话：{}", address, orderDTO.getSenderName(), orderDTO.getSenderPhone());

                title = MessageConstants.MessageTitle.PICKUP;
                contentType = MessageContentTypeEnum.COURIER_PICKUP.getCode();
            }
            var messageAddDTO = MessageAddDTO.builder()
                    .title(title)
                    .content(content)
                    .bussinessType(MessageBussinessTypeEnum.COURIER.getCode())
                    .userId(taskPickupDispatch.getCourierId())
                    .contentType(contentType)
                    .relevantId(taskPickupDispatch.getId())
                    .build();
            messageFeign.add(messageAddDTO);
        }
    }

    /**
     * 发送派件相关运单跟踪消息
     *
     * @param taskPickupDispatch 取派件任务
     */
    private void sendTransportInfoMsg(PickupDispatchTaskEntity taskPickupDispatch) {
        var userDTO = authFeign.getByUserId(taskPickupDispatch.getCourierId());
        var transportOrderEntity = transportOrderService.findByOrderId(taskPickupDispatch.getOrderId());
        var info = StrUtil.format("您的快递正在派送途中，派件人【{}，电话 {}】", userDTO.getName(), userDTO.getMobile());

        //构建消息实体类
        var transportInfoMsg = TransportInfoMsg.builder()
                .transportOrderId(transportOrderEntity.getId())
                .status("派送中")
                .info(info)
                .created(DateUtil.current())
                .build().toJson();
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_INFO, Constants.MQ.RoutingKeys.TRANSPORT_INFO_APPEND, transportInfoMsg);
    }

    /**
     * 同步快递员任务到es
     *
     * @param taskPickupDispatch 取派件任务
     */
    private void syncCourierTask2Es(PickupDispatchTaskEntity taskPickupDispatch) {
        //构建快递员任务
        var courierTaskDTO = this.getCourierTask(taskPickupDispatch);
        //为快递员任务set运单号
        try {
            var transportOrderEntity = transportOrderService.findByOrderId(taskPickupDispatch.getOrderId());
            courierTaskDTO.setTransportOrderId(transportOrderEntity.getId());
        } catch (Exception e) {
            log.info("id为{}的取派件任务没有运单！", taskPickupDispatch.getId());
        }
        //发送消息，快递员任务存入es
        mqFeign.sendMsg(Constants.MQ.Exchanges.COURIER_TASK, Constants.MQ.RoutingKeys.COURIER_TASK_SAVE_OR_UPDATE, JSONUtil.toJsonStr(courierTaskDTO));
    }

    /**
     * 构建快递员任务
     *
     * @return 快递员任务
     */
    private CourierTaskDTO getCourierTask(PickupDispatchTaskEntity taskPickupDispatch) {
        //查询订单
        var orderDTO = orderFeign.findById(taskPickupDispatch.getOrderId());

        //取派件任务字段复制到快递员任务
        var courierTaskDTO = BeanUtil.toBean(taskPickupDispatch, CourierTaskDTO.class);
        courierTaskDTO.setTaskType(taskPickupDispatch.getTaskType().getCode());
        courierTaskDTO.setStatus(taskPickupDispatch.getStatus().getCode());
        courierTaskDTO.setIsDeleted(PickupDispatchTaskIsDeletedEnum.NOT_DELETED.getCode());

        //设置快递员任务的姓名、电话、地址字段
        setNameAndPhoneAndAddress(orderDTO, courierTaskDTO);
        return courierTaskDTO;
    }

    /**
     * 设置快递员任务的姓名、电话、地址字段
     *
     * @param orderDTO       订单
     * @param courierTaskDTO 快递员任务
     */
    private void setNameAndPhoneAndAddress(OrderDTO orderDTO, CourierTaskDTO courierTaskDTO) {
        String name;
        String phone;
        String address;
        if (courierTaskDTO.getTaskType().equals(PickupDispatchTaskTypeEnum.PICKUP.getCode())) {
            name = orderDTO.getSenderName();
            phone = orderDTO.getSenderPhone();
            address = orderDTO.getSenderAddress();
        } else {
            name = orderDTO.getReceiverName();
            phone = orderDTO.getReceiverPhone();
            address = orderDTO.getReceiverAddress();
        }
        courierTaskDTO.setAddress(address);
        courierTaskDTO.setName(name);
        courierTaskDTO.setPhone(phone);
    }

    @Override
    public PageResponse<PickupDispatchTaskDTO> findByPage(PickupDispatchTaskPageQueryDTO dto) {
        var iPage = new Page<PickupDispatchTaskEntity>(dto.getPage(), dto.getPageSize());
        var queryWrapper = Wrappers.<PickupDispatchTaskEntity>lambdaQuery()
                .like(ObjectUtil.isNotEmpty(dto.getId()), PickupDispatchTaskEntity::getId, dto.getId())
                .like(ObjectUtil.isNotEmpty(dto.getOrderId()), PickupDispatchTaskEntity::getOrderId, dto.getOrderId())
                .eq(ObjectUtil.isNotEmpty(dto.getAgencyId()), PickupDispatchTaskEntity::getAgencyId, dto.getAgencyId())
                .eq(ObjectUtil.isNotEmpty(dto.getCourierId()), PickupDispatchTaskEntity::getCourierId, dto.getCourierId())
                .eq(ObjectUtil.isNotEmpty(dto.getTaskType()), PickupDispatchTaskEntity::getTaskType, dto.getTaskType())
                .eq(ObjectUtil.isNotEmpty(dto.getStatus()), PickupDispatchTaskEntity::getStatus, dto.getStatus())
                .eq(ObjectUtil.isNotEmpty(dto.getAssignedStatus()), PickupDispatchTaskEntity::getAssignedStatus, dto.getAssignedStatus())
                .eq(ObjectUtil.isNotEmpty(dto.getSignStatus()), PickupDispatchTaskEntity::getSignStatus, dto.getSignStatus())
                .eq(ObjectUtil.isNotEmpty(dto.getIsDeleted()), PickupDispatchTaskEntity::getIsDeleted, dto.getIsDeleted())
                .between(ObjectUtil.isNotEmpty(dto.getMinEstimatedEndTime()), PickupDispatchTaskEntity::getEstimatedEndTime, dto.getMinEstimatedEndTime(), dto.getMaxEstimatedEndTime())
                .between(ObjectUtil.isNotEmpty(dto.getMinActualEndTime()), PickupDispatchTaskEntity::getActualEndTime, dto.getMinActualEndTime(), dto.getMaxActualEndTime())
                .orderByDesc(PickupDispatchTaskEntity::getUpdated);
        var result = super.page(iPage, queryWrapper);
        return PageResponse.of(result, PickupDispatchTaskDTO.class);
    }

    @Override
    public List<CourierTaskCountDTO> findCountByCourierIds(List<Long> courierIds, PickupDispatchTaskTypeEnum pickupDispatchTaskType, String date) {
        //计算一天的时间的边界
        var dateTime = DateUtil.parse(date);
        var startDateTime = DateUtil.beginOfDay(dateTime).toLocalDateTime();
        var endDateTime = DateUtil.endOfDay(dateTime).toLocalDateTime();
        return this.taskPickupDispatchMapper.findCountByCourierIds(courierIds, pickupDispatchTaskType.getCode(), startDateTime, endDateTime);
    }

    @Override
    public List<PickupDispatchTaskEntity> findByOrderId(Long orderId, PickupDispatchTaskTypeEnum taskType) {
        var queryWrapper = Wrappers.<PickupDispatchTaskEntity>lambdaQuery()
                .eq(PickupDispatchTaskEntity::getOrderId, orderId)
                .eq(PickupDispatchTaskEntity::getTaskType, taskType)
                .orderByAsc(PickupDispatchTaskEntity::getCreated);
        return super.list(queryWrapper);
    }

    // 逻辑删除并同步快递员任务
    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        var list = ids.stream()
                .map(id -> { // 通过id列表构造对象列表
                    var dispatchTaskEntity = new PickupDispatchTaskEntity();
                    dispatchTaskEntity.setId(id);
                    dispatchTaskEntity.setIsDeleted(PickupDispatchTaskIsDeletedEnum.IS_DELETED);

                    //发送消息，同步更新快递员任务
                    var courierTaskDTO = this.courierTaskFeign.findById(id);
                    courierTaskDTO.setIsDeleted(PickupDispatchTaskIsDeletedEnum.IS_DELETED.getCode());
                    courierTaskDTO.setUpdated(LocalDateTime.now());
                    this.mqFeign.sendMsg(Constants.MQ.Exchanges.COURIER_TASK, Constants.MQ.RoutingKeys.COURIER_TASK_SAVE_OR_UPDATE, JSONUtil.toJsonStr(courierTaskDTO));
                    return dispatchTaskEntity;

                })
                .collect(Collectors.toList());
        return super.updateBatchById(list);
    }

    @Override
    public Integer todayTasksCount(Long courierId, PickupDispatchTaskTypeEnum taskType, PickupDispatchTaskStatusEnum status, PickupDispatchTaskIsDeletedEnum isDeleted) {
        var queryWrapper = Wrappers.<PickupDispatchTaskEntity>lambdaQuery()
                .eq(ObjectUtil.isNotEmpty(courierId), PickupDispatchTaskEntity::getCourierId, courierId)
                .eq(ObjectUtil.isNotEmpty(taskType), PickupDispatchTaskEntity::getTaskType, taskType)
                .eq(ObjectUtil.isNotEmpty(status), PickupDispatchTaskEntity::getStatus, status)
                .eq(ObjectUtil.isNotEmpty(isDeleted), PickupDispatchTaskEntity::getIsDeleted, isDeleted);
        var startTime = LocalDateTimeUtil.of(DateUtil.beginOfDay(new Date()));
        var endTime = LocalDateTimeUtil.of(DateUtil.endOfDay(new Date()));
        if (status == null) {
            queryWrapper.between(PickupDispatchTaskEntity::getCreated, startTime, endTime);
        } else if (status == PickupDispatchTaskStatusEnum.NEW) {
            queryWrapper.between(PickupDispatchTaskEntity::getEstimatedEndTime, startTime, endTime);
        } else if (status == PickupDispatchTaskStatusEnum.COMPLETED) {
            queryWrapper.between(PickupDispatchTaskEntity::getActualEndTime, startTime, endTime);
        } else if (status == PickupDispatchTaskStatusEnum.CANCELLED) {
            queryWrapper.between(PickupDispatchTaskEntity::getCancelTime, startTime, endTime);
        }
        return Convert.toInt(super.count(queryWrapper));
    }

    /**
     * 条件查询所有
     *
     * @param courierId  快递员id
     * @param taskType   任务类型，1为取件任务，2为派件任务
     * @param taskStatus 任务状态,1新任务，2已完成，3已取消
     * @param isDeleted  是否逻辑删除
     * @return 取派件任务列表
     */
    @Override
    public List<PickupDispatchTaskDTO> findAll(Long courierId, PickupDispatchTaskTypeEnum
            taskType, PickupDispatchTaskStatusEnum taskStatus, PickupDispatchTaskIsDeletedEnum isDeleted) {
        //构建查询条件
        var queryWrapper = Wrappers.<PickupDispatchTaskEntity>lambdaQuery()
                .eq(ObjectUtil.isNotEmpty(courierId), PickupDispatchTaskEntity::getCourierId, courierId)
                .eq(ObjectUtil.isNotEmpty(taskType), PickupDispatchTaskEntity::getTaskType, taskType)
                .eq(ObjectUtil.isNotEmpty(taskStatus), PickupDispatchTaskEntity::getStatus, taskStatus)
                .eq(ObjectUtil.isNotEmpty(isDeleted), PickupDispatchTaskEntity::getIsDeleted, isDeleted);

        var entities = super.list(queryWrapper);
        return BeanUtil.copyToList(entities, PickupDispatchTaskDTO.class);
    }

    /**
     * 今日任务分类统计
     *
     * @param courierId 快递员id
     * @return 统计结果
     */
    @Override
    public PickupDispatchTaskStatisticsDTO todayTaskStatistics(Long courierId) {
        var taskStatisticsDTO = new PickupDispatchTaskStatisticsDTO();
        //今日取件任务数量
        taskStatisticsDTO.setPickupNum(todayTasksCount(courierId, PickupDispatchTaskTypeEnum.PICKUP, null, PickupDispatchTaskIsDeletedEnum.NOT_DELETED));

        //今日待取件任务数量
        taskStatisticsDTO.setNewPickUpNum(todayTasksCount(courierId, PickupDispatchTaskTypeEnum.PICKUP, PickupDispatchTaskStatusEnum.NEW, PickupDispatchTaskIsDeletedEnum.NOT_DELETED));

        //今日已取件任务数量
        taskStatisticsDTO.setCompletePickUpNum(todayTasksCount(courierId, PickupDispatchTaskTypeEnum.PICKUP, PickupDispatchTaskStatusEnum.COMPLETED, PickupDispatchTaskIsDeletedEnum.NOT_DELETED));

        //今日已取消取件任务数量
        taskStatisticsDTO.setCancelPickUpNum(todayTasksCount(courierId, PickupDispatchTaskTypeEnum.PICKUP, PickupDispatchTaskStatusEnum.CANCELLED, PickupDispatchTaskIsDeletedEnum.NOT_DELETED));

        //今日派件任务数量
        taskStatisticsDTO.setDispatchNum(todayTasksCount(courierId, PickupDispatchTaskTypeEnum.DISPATCH, null, PickupDispatchTaskIsDeletedEnum.NOT_DELETED));

        //今日待派件任务数量
        taskStatisticsDTO.setNewDispatchNum(todayTasksCount(courierId, PickupDispatchTaskTypeEnum.DISPATCH, PickupDispatchTaskStatusEnum.NEW, PickupDispatchTaskIsDeletedEnum.NOT_DELETED));

        //今日已签收任务数量
        taskStatisticsDTO.setSignedNum(todayTasksCount(courierId, PickupDispatchTaskTypeEnum.DISPATCH, PickupDispatchTaskStatusEnum.COMPLETED, PickupDispatchTaskIsDeletedEnum.NOT_DELETED));

        //今日已取消派件任务数量
        taskStatisticsDTO.setCancelDispatchNum(todayTasksCount(courierId, PickupDispatchTaskTypeEnum.DISPATCH, PickupDispatchTaskStatusEnum.CANCELLED, PickupDispatchTaskIsDeletedEnum.NOT_DELETED));
        return taskStatisticsDTO;
    }

    @Override
    public TaskStatsDTO getCourierTaskStats(Long courierId) {
        if (ObjectUtil.isEmpty(courierId)) {
            throw new CCException("快递员ID不能为空", WorkExceptionEnum.PICKUP_DISPATCH_TASK_PARAM_ERROR.getCode());
        }
        var startTime = LocalDateTimeUtil.of(DateUtil.beginOfDay(new Date()));
        var endTime = LocalDateTimeUtil.of(DateUtil.endOfDay(new Date()));
        var queryWrapper = Wrappers.<PickupDispatchTaskEntity>lambdaQuery()
                .eq(PickupDispatchTaskEntity::getCourierId, courierId)
                .between(PickupDispatchTaskEntity::getCreated, startTime, endTime)
                .eq(PickupDispatchTaskEntity::getIsDeleted, PickupDispatchTaskIsDeletedEnum.NOT_DELETED);
        var tasks = super.list(queryWrapper);
        if (CollUtil.isEmpty(tasks)) {
            return TaskStatsDTO.builder()
                    .totalToday(0)
                    .completed(0)
                    .inProgress(0)
                    .pendingPickup(0)
                    .pendingDelivery(0)
                    .taskIds(List.of())
                    .isIdle(true)
                    .idleDurationMin(8 * 60L)
                    .build();
        }
        var totalToday = tasks.size();
        var completed = 0;
        var pendingPickup = 0;
        var pendingDelivery = 0;
        var taskIds = CollStreamUtil.toList(tasks, PickupDispatchTaskEntity::getId);
        LocalDateTime lastTaskEndTime = null;
        for (var task : tasks) {
            if (PickupDispatchTaskStatusEnum.COMPLETED == task.getStatus()) {
                completed++;
                if (task.getCreated() != null && (lastTaskEndTime == null || task.getActualEndTime().isAfter(lastTaskEndTime))) {
                    lastTaskEndTime = task.getActualEndTime();
                }
            }
            if (PickupDispatchTaskTypeEnum.PICKUP == task.getTaskType() && ObjectUtil.isEmpty(task.getActualEndTime())) {
                pendingPickup++;
            }
            if (PickupDispatchTaskTypeEnum.DISPATCH == task.getTaskType() && ObjectUtil.isEmpty(task.getActualEndTime())) {
                pendingDelivery++;
            }
        }
        var inProgress = totalToday - completed;
        var isIdle = (totalToday == 0) || (completed == totalToday);
        Long idleDurationMin = null;
        // 计算空闲时长
        if (isIdle) {
            if (totalToday == 0) {
                idleDurationMin = 8 * 60L;
            } else if (lastTaskEndTime != null) {
                idleDurationMin = LocalDateTimeUtil.between(lastTaskEndTime, LocalDateTime.now()).toMinutes();
            } else {
                idleDurationMin = 0L;
            }
        }
        return TaskStatsDTO.builder()
                .totalToday(totalToday)
                .completed(completed)
                .inProgress(inProgress)
                .pendingPickup(pendingPickup)
                .pendingDelivery(pendingDelivery)
                .taskIds(taskIds)
                .isIdle(isIdle)
                .idleDurationMin(idleDurationMin)
                .build();
    }
}
