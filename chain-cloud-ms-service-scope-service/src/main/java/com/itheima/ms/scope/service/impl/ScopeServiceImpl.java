package com.itheima.ms.scope.service.impl;

import com.itheima.ms.scope.entity.ServiceScopeEntity;
import com.itheima.ms.scope.enums.ServiceTypeEnum;
import com.itheima.ms.scope.service.ScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.stereotype.Service;

import java.util.List;

//TODO 该实现类中的方法都未实现
@Service
@RequiredArgsConstructor
public class ScopeServiceImpl implements ScopeService {
    @Override
    public Boolean saveOrUpdate(Long bid, ServiceTypeEnum type, GeoJsonPolygon polygon) {
        return null;
    }

    @Override
    public Boolean delete(String id) {
        return null;
    }

    @Override
    public Boolean delete(Long bid, ServiceTypeEnum type) {
        return null;
    }

    @Override
    public ServiceScopeEntity queryById(String id) {
        return null;
    }

    @Override
    public ServiceScopeEntity queryByBidAndType(Long bid, ServiceTypeEnum type) {
        return null;
    }

    @Override
    public List<ServiceScopeEntity> queryListByPoint(ServiceTypeEnum type, GeoJsonPoint point) {
        return List.of();
    }

    @Override
    public List<ServiceScopeEntity> queryListByPoint(ServiceTypeEnum type, String address) {
        return List.of();
    }

    @Override
    public List<ServiceScopeEntity> queryAdjacentScopes(Double longitude, Double latitude, ServiceTypeEnum type, Integer limit) {
        return List.of();
    }
}
