package com.itheima.ms.work.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.ms.work.entity.TransportOrderTaskEntity;

import java.util.List;

public interface TransportOrderTaskService extends IService<TransportOrderTaskEntity> {

    void batchSaveTransportOrder(List<TransportOrderTaskEntity> transportOrderTaskList);

    IPage<TransportOrderTaskEntity> findByPage(Integer page, Integer pageSize, String transportOrderId, Long transportTaskId);

    List<TransportOrderTaskEntity> findAll(String transportOrderId, Long transportTaskId);

    Long count(String transportOrderId, Long transportTaskId);

    void del(String transportOrderId, Long transportTaskId);
}
