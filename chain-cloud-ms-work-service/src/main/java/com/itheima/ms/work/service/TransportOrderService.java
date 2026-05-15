package com.itheima.ms.work.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.work.domain.TransportOrderDTO;
import com.itheima.chaincloud.domain.work.domain.request.TransportOrderQueryDTO;
import com.itheima.chaincloud.domain.work.domain.response.TransportOrderStatusCountDTO;
import com.itheima.chaincloud.domain.work.enums.TransportOrderStatusEnum;
import com.itheima.chaincloud.common.vo.OrderMsg;
import com.itheima.ms.work.entity.TransportOrderEntity;

import java.util.List;

public interface TransportOrderService extends IService<TransportOrderEntity> {

    TransportOrderEntity orderToTransportOrder(Long orderId);

    Page<TransportOrderEntity> findByPage(TransportOrderQueryDTO transportOrderQueryDTO);

    TransportOrderEntity findByOrderId(Long orderId);

    List<TransportOrderEntity> findByOrderIds(Long[] orderIds);

    List<TransportOrderEntity> findByIds(String[] ids);

    List<TransportOrderEntity> searchById(String id);

    boolean updateStatus(List<String> ids, TransportOrderStatusEnum transportOrderStatus);

    boolean updateByTaskId(Long taskId);

    List<TransportOrderStatusCountDTO> findStatusCount();

    PageResponse<TransportOrderDTO> pageQueryByTaskId(Integer page, Integer pageSize, String taskId, String transportOrderId);

    void sendPickupDispatchTaskMsgToDispatch(TransportOrderEntity transportOrder, OrderMsg orderMsg);
}
