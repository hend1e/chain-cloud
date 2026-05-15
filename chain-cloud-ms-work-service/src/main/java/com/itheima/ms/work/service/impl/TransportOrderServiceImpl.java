package com.itheima.ms.work.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.chaincloud.api.base.common.MQFeign;
import com.itheima.chaincloud.api.oms.OrderFeign;
import com.itheima.chaincloud.api.transport.OrganFeign;
import com.itheima.chaincloud.api.transport.TransportLineFeign;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.enums.IdEnum;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.service.IdService;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.common.vo.OrderMsg;
import com.itheima.chaincloud.common.vo.TransportOrderMsg;
import com.itheima.chaincloud.common.vo.TransportOrderStatusMsg;
import com.itheima.chaincloud.domain.oms.domain.OrderCargoDTO;
import com.itheima.chaincloud.domain.oms.domain.OrderDetailDTO;
import com.itheima.chaincloud.domain.oms.domain.OrderLocationDTO;
import com.itheima.chaincloud.domain.transport.domain.OrganDTO;
import com.itheima.chaincloud.domain.transport.domain.TransportLineNodeDTO;
import com.itheima.chaincloud.domain.work.domain.TransportOrderDTO;
import com.itheima.chaincloud.domain.work.domain.request.TransportOrderQueryDTO;
import com.itheima.chaincloud.domain.work.domain.response.TransportOrderStatusCountDTO;
import com.itheima.chaincloud.domain.work.enums.*;
import com.itheima.ms.work.entity.TransportOrderEntity;
import com.itheima.ms.work.entity.TransportOrderTaskEntity;
import com.itheima.ms.work.mapper.TransportOrderMapper;
import com.itheima.ms.work.mapper.TransportOrderTaskMapper;
import com.itheima.ms.work.service.TransportOrderService;
import com.itheima.ms.work.service.TransportTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransportOrderServiceImpl extends ServiceImpl<TransportOrderMapper, TransportOrderEntity> implements TransportOrderService {

    @Resource private OrderFeign orderFeign;
    @Resource private TransportLineFeign transportLineFeign;
    @Resource private IdService idService;
    @Resource private MQFeign mqFeign;
    @Resource private OrganFeign organFeign;
    @Resource private TransportTaskService transportTaskService;
    @Resource private TransportOrderTaskMapper transportOrderTaskMapper;

    @Override
    @Transactional
    public TransportOrderEntity orderToTransportOrder(Long orderId) {
        TransportOrderEntity transportOrderEntity = this.findByOrderId(orderId);
        if (ObjectUtil.isNotEmpty(transportOrderEntity)) return transportOrderEntity;
        OrderDetailDTO detailByOrder = this.orderFeign.findDetailByOrderId(orderId);
        if (ObjectUtil.isEmpty(detailByOrder)) throw new CCException(WorkExceptionEnum.ORDER_NOT_FOUND);
        OrderCargoDTO cargoDto = detailByOrder.getOrderDTO().getOrderCargoDto();
        if (ObjectUtil.isEmpty(cargoDto)) throw new CCException(WorkExceptionEnum.ORDER_CARGO_NOT_FOUND);
        OrderLocationDTO orderLocationDTO = detailByOrder.getOrderLocationDTO();
        if (ObjectUtil.isEmpty(orderLocationDTO)) throw new CCException(WorkExceptionEnum.ORDER_LOCATION_NOT_FOUND);
        Long sendAgentId = Convert.toLong(orderLocationDTO.getSendAgentId());
        Long receiveAgentId = Convert.toLong(orderLocationDTO.getReceiveAgentId());
        boolean isDispatch = true;
        TransportLineNodeDTO transportLineNodeDTO = null;
        if (ObjectUtil.equal(sendAgentId, receiveAgentId)) isDispatch = false;
        else {
            transportLineNodeDTO = this.transportLineFeign.queryPathByDispatchMethod(sendAgentId, receiveAgentId);
            if (ObjectUtil.isEmpty(transportLineNodeDTO) || CollUtil.isEmpty(transportLineNodeDTO.getNodeList())) throw new CCException(WorkExceptionEnum.TRANSPORT_LINE_NOT_FOUND);
        }
        TransportOrderEntity transportOrder = new TransportOrderEntity();
        transportOrder.setId(this.idService.getId(IdEnum.TRANSPORT_ORDER));
        transportOrder.setOrderId(orderId);
        transportOrder.setStartAgencyId(sendAgentId);
        transportOrder.setEndAgencyId(receiveAgentId);
        transportOrder.setCurrentAgencyId(sendAgentId);
        if (ObjectUtil.isNotEmpty(transportLineNodeDTO)) {
            transportOrder.setStatus(TransportOrderStatusEnum.CREATED);
            transportOrder.setSchedulingStatus(TransportOrderSchedulingStatusEnum.TO_BE_SCHEDULED);
            transportOrder.setNextAgencyId(transportLineNodeDTO.getNodeList().get(1).getId());
            transportOrder.setTransportLine(JSONUtil.toJsonStr(transportLineNodeDTO));
        } else {
            transportOrder.setNextAgencyId(sendAgentId);
            transportOrder.setStatus(TransportOrderStatusEnum.ARRIVED_END);
            transportOrder.setSchedulingStatus(TransportOrderSchedulingStatusEnum.SCHEDULED);
        }
        transportOrder.setTotalVolume(cargoDto.getVolume());
        transportOrder.setTotalWeight(cargoDto.getWeight());
        transportOrder.setIsRejection(false);
        boolean result = super.save(transportOrder);
        if (result) {
            if (isDispatch) this.sendTransportOrderMsgToDispatch(transportOrder);
            else {
                this.sendUpdateStatusMsg(ListUtil.toList(transportOrder.getId()), TransportOrderStatusEnum.ARRIVED_END);
                this.sendDispatchTaskMsgToDispatch(transportOrder);
            }
            String msg = TransportOrderMsg.builder().id(transportOrder.getId()).orderId(transportOrder.getOrderId()).created(DateUtil.current()).build().toJson();
            this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED, Constants.MQ.RoutingKeys.TRANSPORT_ORDER_CREATE, msg, Constants.MQ.NORMAL_DELAY);
            return transportOrder;
        }
        throw new CCException(WorkExceptionEnum.TRANSPORT_ORDER_SAVE_ERROR);
    }

    private void sendTransportOrderMsgToDispatch(TransportOrderEntity transportOrder) {
        Map<String, Object> msg = MapUtil.<String, Object>builder().put("transportOrderId", transportOrder.getId())
                .put("currentAgencyId", transportOrder.getCurrentAgencyId()).put("nextAgencyId", transportOrder.getNextAgencyId())
                .put("totalWeight", transportOrder.getTotalWeight()).put("totalVolume", transportOrder.getTotalVolume())
                .put("created", System.currentTimeMillis()).build();
        String jsonMsg = JSONUtil.toJsonStr(msg);
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED, Constants.MQ.RoutingKeys.JOIN_DISPATCH, jsonMsg, Constants.MQ.LOW_DELAY);
    }

    private void sendDispatchTaskMsgToDispatch(TransportOrderEntity transportOrder) {
        int offset = 0;
        if (LocalDateTime.now().getHour() >= 12) offset = 1;
        LocalDateTime estimatedEndTime = DateUtil.offsetDay(new Date(), offset)
                .setField(DateField.HOUR_OF_DAY, 22).setField(DateField.MINUTE, 0)
                .setField(DateField.SECOND, 0).setField(DateField.MILLISECOND, 0).toLocalDateTime();
        OrderMsg orderMsg = OrderMsg.builder().agencyId(transportOrder.getCurrentAgencyId()).orderId(transportOrder.getOrderId())
                .created(DateUtil.current()).taskType(PickupDispatchTaskTypeEnum.DISPATCH.getCode())
                .mark("系统提示：派件前请于收件人电话联系.").estimatedEndTime(estimatedEndTime).build();
        this.sendPickupDispatchTaskMsgToDispatch(transportOrder, orderMsg);
    }

    @Override
    public void sendPickupDispatchTaskMsgToDispatch(TransportOrderEntity transportOrder, OrderMsg orderMsg) {
        OrderLocationDTO orderLocationDTO = this.orderFeign.findOrderLocationByOrderId(orderMsg.getOrderId());
        String location;
        if (ObjectUtil.isEmpty(transportOrder)) location = orderLocationDTO.getSendLocation();
        else location = transportOrder.getIsRejection() ? orderLocationDTO.getSendLocation() : orderLocationDTO.getReceiveLocation();
        Double[] coordinate = Convert.convert(Double[].class, StrUtil.split(location, ","));
        orderMsg.setLongitude(coordinate[0]);
        orderMsg.setLatitude(coordinate[1]);
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.ORDER_DELAYED, Constants.MQ.RoutingKeys.ORDER_CREATE, orderMsg.toJson(), Constants.MQ.NORMAL_DELAY);
    }

    private void sendUpdateStatusMsg(List<String> ids, TransportOrderStatusEnum transportOrderStatus) {
        String msg = TransportOrderStatusMsg.builder().idList(ids).statusName(transportOrderStatus.name()).statusCode(transportOrderStatus.getCode()).build().toJson();
        String routingKey = Constants.MQ.RoutingKeys.TRANSPORT_ORDER_UPDATE_STATUS_PREFIX + transportOrderStatus.name();
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED, routingKey, msg, Constants.MQ.LOW_DELAY);
    }

    @Override
    public Page<TransportOrderEntity> findByPage(TransportOrderQueryDTO transportOrderQueryDTO) {
        Page<TransportOrderEntity> iPage = new Page<>(transportOrderQueryDTO.getPage(), transportOrderQueryDTO.getPageSize());
        LambdaQueryWrapper<TransportOrderEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getId()), TransportOrderEntity::getId, transportOrderQueryDTO.getId());
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getOrderId()), TransportOrderEntity::getOrderId, transportOrderQueryDTO.getOrderId());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getStatus()), TransportOrderEntity::getStatus, transportOrderQueryDTO.getStatus());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getSchedulingStatus()), TransportOrderEntity::getSchedulingStatus, transportOrderQueryDTO.getSchedulingStatus());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getStartAgencyId()), TransportOrderEntity::getStartAgencyId, transportOrderQueryDTO.getStartAgencyId());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getEndAgencyId()), TransportOrderEntity::getEndAgencyId, transportOrderQueryDTO.getEndAgencyId());
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(transportOrderQueryDTO.getCurrentAgencyId()), TransportOrderEntity::getCurrentAgencyId, transportOrderQueryDTO.getCurrentAgencyId());
        lambdaQueryWrapper.orderByDesc(TransportOrderEntity::getCreated);
        return super.page(iPage, lambdaQueryWrapper);
    }

    @Override
    public TransportOrderEntity findByOrderId(Long orderId) {
        LambdaQueryWrapper<TransportOrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TransportOrderEntity::getOrderId, orderId);
        return super.getOne(queryWrapper);
    }

    @Override
    public List<TransportOrderEntity> findByOrderIds(Long[] orderIds) {
        LambdaQueryWrapper<TransportOrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TransportOrderEntity::getOrderId, orderIds);
        return super.list(queryWrapper);
    }

    @Override
    public List<TransportOrderEntity> findByIds(String[] ids) {
        LambdaQueryWrapper<TransportOrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TransportOrderEntity::getId, ids);
        return super.list(queryWrapper);
    }

    @Override
    public List<TransportOrderEntity> searchById(String id) {
        LambdaQueryWrapper<TransportOrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(TransportOrderEntity::getId, id);
        return super.list(queryWrapper);
    }

    @Override
    public boolean updateStatus(List<String> ids, TransportOrderStatusEnum transportOrderStatus) {
        if (CollUtil.isEmpty(ids)) return false;
        if (TransportOrderStatusEnum.CREATED == transportOrderStatus) throw new CCException(WorkExceptionEnum.TRANSPORT_ORDER_STATUS_NOT_CREATED);
        List<TransportOrderEntity> transportOrderList;
        if (TransportOrderStatusEnum.REJECTED == transportOrderStatus) {
            transportOrderList = super.listByIds(ids);
            for (TransportOrderEntity transportOrderEntity : transportOrderList) {
                transportOrderEntity.setIsRejection(true);
                Long sendAgentId = transportOrderEntity.getEndAgencyId();
                Long receiveAgentId = transportOrderEntity.getStartAgencyId();
                boolean isDispatch = true;
                if (ObjectUtil.equal(sendAgentId, receiveAgentId)) isDispatch = false;
                else {
                    TransportLineNodeDTO transportLineNodeDTO = this.transportLineFeign.queryPathByDispatchMethod(sendAgentId, receiveAgentId);
                    if (ObjectUtil.hasEmpty(transportLineNodeDTO, transportLineNodeDTO.getNodeList())) throw new CCException(WorkExceptionEnum.TRANSPORT_LINE_NOT_FOUND);
                    transportLineNodeDTO.getNodeList().remove(0);
                    transportOrderEntity.setSchedulingStatus(TransportOrderSchedulingStatusEnum.TO_BE_SCHEDULED);
                    transportOrderEntity.setCurrentAgencyId(sendAgentId);
                    transportOrderEntity.setNextAgencyId(transportLineNodeDTO.getNodeList().get(0).getId());
                    TransportLineNodeDTO transportLineNode = JSONUtil.toBean(transportOrderEntity.getTransportLine(), TransportLineNodeDTO.class);
                    transportLineNode.getNodeList().addAll(transportLineNodeDTO.getNodeList());
                    transportLineNode.setCost(NumberUtil.add(transportLineNode.getCost(), transportLineNodeDTO.getCost()));
                    transportOrderEntity.setTransportLine(JSONUtil.toJsonStr(transportLineNode));
                }
                transportOrderEntity.setStatus(TransportOrderStatusEnum.REJECTED);
                if (isDispatch) this.sendTransportOrderMsgToDispatch(transportOrderEntity);
                else { transportOrderEntity.setStatus(TransportOrderStatusEnum.ARRIVED_END); this.sendDispatchTaskMsgToDispatch(transportOrderEntity); }
            }
        } else {
            transportOrderList = ids.stream().map(id -> {
                TransportOrderEntity transportOrderEntity = new TransportOrderEntity();
                transportOrderEntity.setId(id);
                transportOrderEntity.setStatus(transportOrderStatus);
                return transportOrderEntity;
            }).collect(Collectors.toList());
        }
        boolean result = super.updateBatchById(transportOrderList);
        this.sendUpdateStatusMsg(ids, transportOrderStatus);
        return result;
    }

    @Override
    public boolean updateByTaskId(Long taskId) {
        var transportOrderIdList = this.transportTaskService.queryTransportOrderIdListById(taskId);
        if (CollUtil.isEmpty(transportOrderIdList)) return false;
        var transportOrderList = super.listByIds(transportOrderIdList);
        for (var transportOrder : transportOrderList) {
            var organDTO = organFeign.queryById(transportOrder.getNextAgencyId());
            transportOrder.setCurrentAgencyId(transportOrder.getNextAgencyId());
            var transportLine = transportOrder.getTransportLine();
            var transportLineNodeDTO = JSONUtil.toBean(transportLine, TransportLineNodeDTO.class);
            var nextAgencyId = 0L;
            var nodeList = transportLineNodeDTO.getNodeList();
            for (var i = nodeList.size() - 1; i >= 0; i--) {
                var node = nodeList.get(i);
                var agencyId = node.getId();
                if (ObjectUtil.equal(agencyId, transportOrder.getCurrentAgencyId())) {
                    if (i == nodeList.size() - 1) { nextAgencyId = agencyId; transportOrder.setStatus(TransportOrderStatusEnum.ARRIVED_END); this.sendUpdateStatusMsg(List.of(transportOrder.getId()), TransportOrderStatusEnum.ARRIVED_END); }
                    else { nextAgencyId = nodeList.get(i + 1).getId(); transportOrder.setSchedulingStatus(TransportOrderSchedulingStatusEnum.TO_BE_SCHEDULED); }
                    break;
                }
            }
            transportOrder.setNextAgencyId(nextAgencyId);
            if (ObjectUtil.notEqual(transportOrder.getStatus(), TransportOrderStatusEnum.ARRIVED_END)) this.sendTransportOrderMsgToDispatch(transportOrder);
            else this.sendDispatchTaskMsgToDispatch(transportOrder);
        }
        return super.updateBatchById(transportOrderList);
    }

    @Override
    public List<TransportOrderStatusCountDTO> findStatusCount() {
        List<TransportOrderStatusCountDTO> statusCountList = StreamUtil.of(TransportOrderStatusEnum.values())
                .map(transportOrderStatus -> TransportOrderStatusCountDTO.builder().status(transportOrderStatus).statusCode(transportOrderStatus.getCode()).count(0L).build())
                .collect(Collectors.toList());
        List<TransportOrderStatusCountDTO> statusCount = super.baseMapper.findStatusCount();
        if (CollUtil.isEmpty(statusCount)) return statusCountList;
        for (TransportOrderStatusCountDTO transportOrderStatusCountDTO : statusCountList) {
            for (TransportOrderStatusCountDTO countDTO : statusCount) {
                if (ObjectUtil.equal(transportOrderStatusCountDTO.getStatusCode(), countDTO.getStatusCode())) { transportOrderStatusCountDTO.setCount(countDTO.getCount()); break; }
            }
        }
        return statusCountList;
    }

    @Override
    public PageResponse<TransportOrderDTO> pageQueryByTaskId(Integer page, Integer pageSize, String taskId, String transportOrderId) {
        var transportOrderTaskPage = new Page<TransportOrderTaskEntity>(page, pageSize);
        var queryWrapper = Wrappers.<TransportOrderTaskEntity>lambdaQuery()
                .eq(ObjectUtil.isNotEmpty(taskId), TransportOrderTaskEntity::getTransportTaskId, taskId)
                .like(ObjectUtil.isNotEmpty(transportOrderId), TransportOrderTaskEntity::getTransportOrderId, transportOrderId)
                .orderByDesc(TransportOrderTaskEntity::getCreated);
        var pageResult = this.transportOrderTaskMapper.selectPage(transportOrderTaskPage, queryWrapper);
        if (ObjectUtil.isEmpty(pageResult.getRecords())) return new PageResponse<>(pageResult);
        var transportOrderIds = CollStreamUtil.toList(pageResult.getRecords(), TransportOrderTaskEntity::getTransportOrderId);
        var entities = super.baseMapper.selectBatchIds(transportOrderIds);
        return PageResponse.of(BeanUtil.copyToList(entities, TransportOrderDTO.class), page, pageSize, pageResult.getPages(), pageResult.getTotal());
    }
}
