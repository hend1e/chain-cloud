package com.itheima.ms.work.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.ms.work.entity.TransportOrderTaskEntity;
import com.itheima.ms.work.mapper.TransportOrderTaskMapper;
import com.itheima.ms.work.service.TransportOrderTaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransportOrderTaskServiceImpl extends
        ServiceImpl<TransportOrderTaskMapper, TransportOrderTaskEntity> implements TransportOrderTaskService {

    @Override
    public void batchSaveTransportOrder(List<TransportOrderTaskEntity> transportOrderTaskList) {
        super.saveBatch(transportOrderTaskList);
    }

    @Override
    public IPage<TransportOrderTaskEntity> findByPage(Integer page, Integer pageSize, String transportOrderId, Long transportTaskId) {
        var iPage = new Page<TransportOrderTaskEntity>(page, pageSize);
        var lambdaQueryWrapper = new LambdaQueryWrapper<TransportOrderTaskEntity>();
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(transportOrderId), TransportOrderTaskEntity::getTransportOrderId, transportOrderId);
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(transportTaskId), TransportOrderTaskEntity::getTransportTaskId, transportTaskId);
        return super.page(iPage, lambdaQueryWrapper);
    }

    @Override
    public List<TransportOrderTaskEntity> findAll(String transportOrderId, Long transportTaskId) {
        var lambdaQueryWrapper = new LambdaQueryWrapper<TransportOrderTaskEntity>();
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(transportOrderId), TransportOrderTaskEntity::getTransportOrderId, transportOrderId);
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(transportTaskId), TransportOrderTaskEntity::getTransportTaskId, transportTaskId);
        lambdaQueryWrapper.orderBy(true, false, TransportOrderTaskEntity::getCreated);
        return super.list(lambdaQueryWrapper);
    }

    @Override
    public Long count(String transportOrderId, Long transportTaskId) {
        var lambdaQueryWrapper = new LambdaQueryWrapper<TransportOrderTaskEntity>();
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(transportOrderId), TransportOrderTaskEntity::getTransportOrderId, transportOrderId);
        lambdaQueryWrapper.like(ObjectUtil.isNotEmpty(transportTaskId), TransportOrderTaskEntity::getTransportTaskId, transportTaskId);
        return super.count(lambdaQueryWrapper);
    }

    @Override
    public void del(String transportOrderId, Long transportTaskId) {
        if (ObjectUtil.isAllEmpty(transportOrderId, transportTaskId)) {
            return;
        }
        var lambdaQueryWrapper = new LambdaQueryWrapper<TransportOrderTaskEntity>();
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(transportOrderId), TransportOrderTaskEntity::getTransportOrderId, transportOrderId);
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(transportTaskId), TransportOrderTaskEntity::getTransportTaskId, transportTaskId);
        super.remove(lambdaQueryWrapper);
    }
}
