package com.itheima.ms.work.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
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
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.service.IdService;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.common.vo.OrderMsg;
import com.itheima.chaincloud.common.vo.TransportInfoMsg;
import com.itheima.chaincloud.common.vo.TransportOrderStatusMsg;
import com.itheima.chaincloud.domain.transport.domain.OrganDTO;
import com.itheima.chaincloud.domain.transport.domain.TransportLineNodeDTO;
import com.itheima.chaincloud.domain.work.domain.TransportOrderDTO;
import com.itheima.chaincloud.domain.work.domain.request.TransportOrderQueryDTO;
import com.itheima.chaincloud.domain.work.domain.response.TransportOrderStatusCountDTO;
import com.itheima.chaincloud.domain.work.enums.PickupDispatchTaskTypeEnum;
import com.itheima.chaincloud.domain.work.enums.TransportOrderSchedulingStatusEnum;
import com.itheima.chaincloud.domain.work.enums.TransportOrderStatusEnum;
import com.itheima.chaincloud.domain.work.enums.WorkExceptionEnum;
import com.itheima.ms.work.entity.TransportOrderEntity;
import com.itheima.ms.work.entity.TransportOrderTaskEntity;
import com.itheima.ms.work.mapper.TransportOrderMapper;
import com.itheima.ms.work.mapper.TransportOrderTaskMapper;
import com.itheima.ms.work.service.TransportOrderService;
import com.itheima.ms.work.service.TransportTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportOrderServiceImpl extends ServiceImpl<TransportOrderMapper, TransportOrderEntity> implements TransportOrderService {

    private final OrderFeign orderFeign;
    private final TransportLineFeign transportLineFeign;
    private final IdService idService;
    private final MQFeign mqFeign;
    private final OrganFeign organFeign;
    private final TransportTaskService transportTaskService;
    private final TransportOrderTaskMapper transportOrderTaskMapper;

    // 订单转运单
    @Override
    @Transactional
    public TransportOrderEntity orderToTransportOrder(Long orderId) {
        // TODO 待实现
        return null;
    }

    /**
     * 发送运单消息到调度中，参与调度
     */
    private void sendTransportOrderMsgToDispatch(TransportOrderEntity transportOrder) {
        var msg = MapUtil.<String, Object>builder()
                .put("transportOrderId", transportOrder.getId())
                .put("currentAgencyId", transportOrder.getCurrentAgencyId())
                .put("nextAgencyId", transportOrder.getNextAgencyId())
                .put("totalWeight", transportOrder.getTotalWeight())
                .put("totalVolume", transportOrder.getTotalVolume())
                .put("created", System.currentTimeMillis())
                .build();
        var jsonMsg = JSONUtil.toJsonStr(msg);
        //发送消息，延迟5秒，确保本地事务已经提交，可以查询到数据
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED,
                Constants.MQ.RoutingKeys.JOIN_DISPATCH, jsonMsg, Constants.MQ.LOW_DELAY);
    }

    // 发送派件调度消息
    private void sendDispatchTaskMsgToDispatch(TransportOrderEntity transportOrder) {
        //预计完成时间，如果是中午12点到的快递，当天22点前，否则，第二天22点前
        var offset = 0;
        if (LocalDateTime.now().getHour() >= 12) {
            offset = 1;
        }
        var estimatedEndTime = DateUtil.offsetDay(new Date(), offset)
                .setField(DateField.HOUR_OF_DAY, 22)
                .setField(DateField.MINUTE, 0)
                .setField(DateField.SECOND, 0)
                .setField(DateField.MILLISECOND, 0)
                .toLocalDateTime();

        //发送分配快递员派件任务的消息
        var orderMsg = OrderMsg.builder()
                .agencyId(transportOrder.getCurrentAgencyId())
                .orderId(transportOrder.getOrderId())
                .created(DateUtil.current())
                .taskType(PickupDispatchTaskTypeEnum.DISPATCH.getCode()) //派件任务
                .mark("系统提示：派件前请于收件人电话联系.")
                .estimatedEndTime(estimatedEndTime)
                .build();

        //发送消息
        this.sendPickupDispatchTaskMsgToDispatch(transportOrder, orderMsg);
    }

    /**
     * 发送消息到调度中心，用于生成取派件任务
     */
    @Override
    public void sendPickupDispatchTaskMsgToDispatch(TransportOrderEntity transportOrder, OrderMsg orderMsg) {
        //查询订单对应的位置信息
        var orderLocationDTO = this.orderFeign.findOrderLocationByOrderId(orderMsg.getOrderId());

        //(1)运单为空：取件任务取消，取消原因为返回网点；重新调度位置取寄件人位置
        //(2)运单不为空：生成的是派件任务，需要根据拒收状态判断位置是寄件人还是收件人
        // 拒收：寄件人  其他：收件人
        String location;
        if (ObjectUtil.isEmpty(transportOrder)) {
            location = orderLocationDTO.getSendLocation();
        } else {
            location = transportOrder.getIsRejection() ? orderLocationDTO.getSendLocation() : orderLocationDTO.getReceiveLocation();
        }

        var coordinate = Convert.convert(Double[].class, StrUtil.split(location, ","));

        //设置消息中的位置信息
        orderMsg.setLongitude(coordinate[0]);
        orderMsg.setLatitude(coordinate[1]);

        //发送消息,用于生成取派件任务
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.ORDER_DELAYED, Constants.MQ.RoutingKeys.ORDER_CREATE,
                orderMsg.toJson(), Constants.MQ.NORMAL_DELAY);
    }

    // 发送运单状态更新消息
    private void sendUpdateStatusMsg(List<String> ids, TransportOrderStatusEnum transportOrderStatus) {
        var msg = TransportOrderStatusMsg.builder()
                .idList(ids)
                .statusName(transportOrderStatus.name())
                .statusCode(transportOrderStatus.getCode())
                .build()
                .toJson();

        //将状态名称写入到路由key中，方便消费方选择性的接收消息
        var routingKey = Constants.MQ.RoutingKeys.TRANSPORT_ORDER_UPDATE_STATUS_PREFIX + transportOrderStatus.name();
        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED, routingKey, msg, Constants.MQ.LOW_DELAY);
    }

    @Override
    public Page<TransportOrderEntity> findByPage(TransportOrderQueryDTO transportOrderQueryDTO) {
        var iPage = new Page<TransportOrderEntity>(transportOrderQueryDTO.getPage(), transportOrderQueryDTO.getPageSize());

        //设置查询条件
        var lambdaQueryWrapper = new LambdaQueryWrapper<TransportOrderEntity>();
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
        var queryWrapper = new LambdaQueryWrapper<TransportOrderEntity>();
        queryWrapper.eq(TransportOrderEntity::getOrderId, orderId);
        return super.getOne(queryWrapper);
    }

    @Override
    public List<TransportOrderEntity> findByOrderIds(Long[] orderIds) {
        var queryWrapper = new LambdaQueryWrapper<TransportOrderEntity>();
        queryWrapper.in(TransportOrderEntity::getOrderId, orderIds);
        return super.list(queryWrapper);
    }

    @Override
    public List<TransportOrderEntity> findByIds(String[] ids) {
        var queryWrapper = new LambdaQueryWrapper<TransportOrderEntity>();
        queryWrapper.in(TransportOrderEntity::getId, ids);
        return super.list(queryWrapper);
    }

    @Override
    public List<TransportOrderEntity> searchById(String id) {
        var queryWrapper = new LambdaQueryWrapper<TransportOrderEntity>();
        queryWrapper.like(TransportOrderEntity::getId, id);
        return super.list(queryWrapper);
    }

    @Override
    public boolean updateStatus(List<String> ids, TransportOrderStatusEnum transportOrderStatus) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        if (TransportOrderStatusEnum.CREATED == transportOrderStatus) {
            //修改订单状态不能为新建状态
            throw new CCException(WorkExceptionEnum.TRANSPORT_ORDER_STATUS_NOT_CREATED);
        }

        List<TransportOrderEntity> transportOrderList;
        //判断是否为拒收状态，如果是拒收需要重新查询路线，将包裹逆向回去
        if (TransportOrderStatusEnum.REJECTED == transportOrderStatus) {
            //查询运单列表
            transportOrderList = super.listByIds(ids);
            for (var transportOrderEntity : transportOrderList) {
                //设置为拒收运单
                transportOrderEntity.setIsRejection(true);
                //根据起始机构规划运输路线，这里要将起点和终点互换
                var sendAgentId = transportOrderEntity.getEndAgencyId();//起始网点id
                var receiveAgentId = transportOrderEntity.getStartAgencyId();//终点网点id

                //默认参与调度
                var isDispatch = true;
                if (ObjectUtil.equal(sendAgentId, receiveAgentId)) {
                    //相同节点，无需调度，直接生成派件任务
                    isDispatch = false;
                } else {
                    var transportLineNodeDTO = this.transportLineFeign.queryPathByDispatchMethod(sendAgentId, receiveAgentId);
                    if (ObjectUtil.hasEmpty(transportLineNodeDTO, transportLineNodeDTO.getNodeList())) {
                        throw new CCException(WorkExceptionEnum.TRANSPORT_LINE_NOT_FOUND);
                    }

                    //删除掉第一个机构，逆向回去的第一个节点就是当前所在节点
                    transportLineNodeDTO.getNodeList().remove(0);
                    transportOrderEntity.setSchedulingStatus(TransportOrderSchedulingStatusEnum.TO_BE_SCHEDULED);
                    transportOrderEntity.setCurrentAgencyId(sendAgentId);
                    transportOrderEntity.setNextAgencyId(transportLineNodeDTO.getNodeList().get(0).getId());

                    //获取到原有节点信息
                    var transportLineNode = JSONUtil.toBean(transportOrderEntity.getTransportLine(), TransportLineNodeDTO.class);
                    //将逆向节点追加到节点列表中
                    transportLineNode.getNodeList().addAll(transportLineNodeDTO.getNodeList());
                    //合并成本
                    transportLineNode.setCost(NumberUtil.add(transportLineNode.getCost(), transportLineNodeDTO.getCost()));
                    transportOrderEntity.setTransportLine(JSONUtil.toJsonStr(transportLineNode));
                }
                transportOrderEntity.setStatus(TransportOrderStatusEnum.REJECTED);

                if (isDispatch) {
                    //发送消息参与调度
                    this.sendTransportOrderMsgToDispatch(transportOrderEntity);
                } else {
                    //不需要调度，发送消息生成派件任务
                    transportOrderEntity.setStatus(TransportOrderStatusEnum.ARRIVED_END);
                    this.sendDispatchTaskMsgToDispatch(transportOrderEntity);
                }
            }
        } else {
            //根据id列表封装成运单对象列表
            transportOrderList = ids.stream()
                    .map(id -> {
                        //获取将发往的目的地机构
                        var nextAgencyId = this.getById(id).getNextAgencyId();
                        var organDTO = this.organFeign.queryById(nextAgencyId);

                        //构建消息实体类
                        var info = StrUtil.format("快件发往【{}】", organDTO.getName());
                        var transportInfoMsg = TransportInfoMsg.builder()
                                .transportOrderId(id)//运单id
                                .status("运送中")//消息状态
                                .info(info)//消息详情
                                .created(DateUtil.current())//创建时间
                                .build()
                                .toJson();
                        //发送运单跟踪消息
                        this.mqFeign.sendMsg(Constants.MQ.Exchanges.TRANSPORT_INFO,
                                Constants.MQ.RoutingKeys.TRANSPORT_INFO_APPEND, transportInfoMsg);

                        //封装运单对象
                        var transportOrderEntity = new TransportOrderEntity();
                        transportOrderEntity.setId(id);
                        transportOrderEntity.setStatus(transportOrderStatus);
                        return transportOrderEntity;
                    })
                    .collect(Collectors.toList());
        }

        //批量更新数据
        var result = super.updateBatchById(transportOrderList);

        //发消息通知其他系统运单状态的变化
        this.sendUpdateStatusMsg(ids, transportOrderStatus);

        return result;
    }

    @Override
    public boolean updateByTaskId(Long taskId) {
        // TODO 待实现
        return false;
    }

    @Override
    public List<TransportOrderStatusCountDTO> findStatusCount() {
        //将所有的枚举状态放到集合中
        var statusCountList = Arrays.stream(TransportOrderStatusEnum.values())
                .map(transportOrderStatus -> TransportOrderStatusCountDTO.builder()
                        .status(transportOrderStatus)
                        .statusCode(transportOrderStatus.getCode())
                        .count(0L)
                        .build())
                .collect(Collectors.toList());

        //将数量值放入到集合中，如果没有的数量为0
        var statusCount = super.baseMapper.findStatusCount();
        if (CollUtil.isEmpty(statusCount)) {
            return statusCountList;
        }
        for (var transportOrderStatusCountDTO : statusCountList) {
            for (var countDTO : statusCount) {
                if (ObjectUtil.equal(transportOrderStatusCountDTO.getStatusCode(), countDTO.getStatusCode())) {
                    transportOrderStatusCountDTO.setCount(countDTO.getCount());
                    break;
                }
            }
        }
        return statusCountList;
    }

    @Override
    public PageResponse<TransportOrderDTO> pageQueryByTaskId(Integer page, Integer pageSize, String taskId, String transportOrderId) {
        //构建分页查询条件
        var transportOrderTaskPage = new Page<TransportOrderTaskEntity>(page, pageSize);
        var queryWrapper = Wrappers.<TransportOrderTaskEntity>lambdaQuery()
                .eq(ObjectUtil.isNotEmpty(taskId), TransportOrderTaskEntity::getTransportTaskId, taskId)
                .like(ObjectUtil.isNotEmpty(transportOrderId), TransportOrderTaskEntity::getTransportOrderId, transportOrderId)
                .orderByDesc(TransportOrderTaskEntity::getCreated);

        //根据运输任务id、运单id查询运输任务与运单关联关系表
        var pageResult = this.transportOrderTaskMapper.selectPage(transportOrderTaskPage, queryWrapper);
        if (ObjectUtil.isEmpty(pageResult.getRecords())) {
            return new PageResponse<>(pageResult);
        }

        //根据运单id查询运单，并转化为dto
        var transportOrderIds = pageResult.getRecords().stream()
                .map(TransportOrderTaskEntity::getTransportOrderId)
                .collect(Collectors.toList());
        var entities = super.baseMapper.selectBatchIds(transportOrderIds);

        //构建分页结果
        return PageResponse.of(BeanUtil.copyToList(entities, TransportOrderDTO.class), page, pageSize, pageResult.getPages(), pageResult.getTotal());
    }
}
