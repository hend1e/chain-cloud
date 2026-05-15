package com.itheima.ms.scope.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import com.itheima.em.sdk.EagleMapTemplate;
import com.itheima.em.sdk.enums.ProviderEnum;
import com.itheima.em.sdk.vo.Coordinate;
import com.itheima.em.sdk.vo.GeoResult;
import com.itheima.ms.scope.entity.ServiceScopeEntity;
import com.itheima.ms.scope.enums.ServiceTypeEnum;
import com.itheima.ms.scope.service.ScopeService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class ScopeServiceImpl implements ScopeService {

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private EagleMapTemplate eagleMapTemplate;

    @Override
    public Boolean saveOrUpdate(Long bid, ServiceTypeEnum type, GeoJsonPolygon polygon) {
        Query query = Query.query(Criteria.where("bid").is(bid).and("type").is(type.getCode())); //构造查询条件
        ServiceScopeEntity serviceScopeEntity = this.mongoTemplate.findOne(query, ServiceScopeEntity.class);
        if (ObjectUtil.isEmpty(serviceScopeEntity)) {
            //新增
            serviceScopeEntity = new ServiceScopeEntity();
            serviceScopeEntity.setBid(bid);
            serviceScopeEntity.setType(type.getCode());
            serviceScopeEntity.setPolygon(polygon);
            serviceScopeEntity.setCreated(System.currentTimeMillis());
            serviceScopeEntity.setUpdated(serviceScopeEntity.getCreated());
        } else {
            //更新
            serviceScopeEntity.setPolygon(polygon);
            serviceScopeEntity.setUpdated(System.currentTimeMillis());
        }

        try {
            this.mongoTemplate.save(serviceScopeEntity);
            return true;
        } catch (Exception e) {
            log.error("新增/更新服务范围数据失败！ bid = {}, type = {}, points = {}", bid, type, polygon.getPoints(), e);
        }
        return false;
    }

    @Override
    public Boolean delete(String id) {
        Query query = Query.query(Criteria.where("id").is(new ObjectId(id))); //构造查询条件
        return this.mongoTemplate.remove(query, ServiceScopeEntity.class).getDeletedCount() > 0;
    }

    @Override
    public Boolean delete(Long bid, ServiceTypeEnum type) {
        Query query = Query.query(Criteria.where("bid").is(bid).and("type").is(type.getCode())); //构造查询条件
        return this.mongoTemplate.remove(query, ServiceScopeEntity.class).getDeletedCount() > 0;
    }

    @Override
    public ServiceScopeEntity queryById(String id) {
        return this.mongoTemplate.findById(new ObjectId(id), ServiceScopeEntity.class);
    }

    @Override
    public ServiceScopeEntity queryByBidAndType(Long bid, ServiceTypeEnum type) {
        Query query = Query.query(Criteria.where("bid").is(bid).and("type").is(type.getCode())); //构造查询条件
        return this.mongoTemplate.findOne(query, ServiceScopeEntity.class);
    }

    @Override
    public List<ServiceScopeEntity> queryListByPoint(ServiceTypeEnum type, GeoJsonPoint point) {
        Query query = Query.query(Criteria.where("polygon")
                .intersects(point)
                .and("type").is(type.getCode()));
        return this.mongoTemplate.find(query, ServiceScopeEntity.class);
    }

    @Override
    public List<ServiceScopeEntity> queryListByPoint(ServiceTypeEnum type, String address) {
        //根据详细地址查询坐标
        GeoResult geoResult = this.eagleMapTemplate.opsForBase().geoCode(ProviderEnum.AMAP, address, null);
        Coordinate coordinate = geoResult.getLocation();
        return this.queryListByPoint(type, new GeoJsonPoint(coordinate.getLongitude(), coordinate.getLatitude()));
    }

    @Override
    public List<ServiceScopeEntity> queryAdjacentScopes(Double longitude, Double latitude, ServiceTypeEnum type, Integer limit) {
        if (limit == null || limit <= 0) {
            // 默认限制数量为10
            limit = 10;
        }

        // 首先找到包含给定坐标点的服务范围
        var point = new GeoJsonPoint(longitude, latitude);
        var containingScopes = this.queryListByPoint(type, point);

        if (containingScopes.isEmpty()) {
            // 如果没有找到包含该点的服务范围，返回空列表
            return containingScopes;
        }

        // 获取当前服务范围的bid（排除自己）
        var excludeBids = CollStreamUtil.toList(containingScopes, ServiceScopeEntity::getBid);

        var currentScope = containingScopes.get(0); // 通常一个点只属于一个服务范围

        // 创建查询：查找与当前服务范围边界相交或邻近的其他服务范围
        var query = new Query();
        var criteria = Criteria.where("type").is(type.getCode()).and("bid").nin(excludeBids);

        // 使用geoIntersects查询与当前多边形相交的范围（直接相邻）
        // 同时使用geoNear来查找附近的多边形
        var geoCriteria = Criteria.where("polygon").intersects(currentScope.getPolygon());
        criteria.andOperator(geoCriteria);

        query.addCriteria(criteria);

        // 设置结果限制
        query.limit(10);

        var adjacentByIntersection = this.mongoTemplate.find(query, ServiceScopeEntity.class);

        // 如果通过相交查询的结果不够，再查找附近的范围
        if (adjacentByIntersection.size() < limit) {
            var nearbyQuery = Query.query(Criteria.where("type").is(type.getCode()).and("bid").nin(excludeBids));

            // 扩大查询范围，查找当前点附近的其他多边形
            // 创建一个以当前点为中心的圆形区域进行查询
            var center = new Point(longitude, latitude);
            // 绘制半径
            Distance distance = new Distance(20, Metrics.KILOMETERS);
            var nearQuery = NearQuery.near(point).maxDistance(distance).query(nearbyQuery).limit(limit);
            var nearbyScopes = this.mongoTemplate.geoNear(nearQuery, ServiceScopeEntity.class);

            // 合并结果并去重
            for (var content : nearbyScopes.getContent()) {
                var nearby = content.getContent();
                boolean isDuplicate = false;
                for (var adjacent : adjacentByIntersection) {
                    if (adjacent.getId().equals(nearby.getId())) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (!isDuplicate) {
                    adjacentByIntersection.add(nearby);
                }

                if (adjacentByIntersection.size() >= limit) {
                    break;
                }
            }
        }

        return adjacentByIntersection;
    }
}
