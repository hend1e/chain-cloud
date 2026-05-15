package com.itheima.ms.scope.service;

import com.itheima.ms.scope.entity.ServiceScopeEntity;
import com.itheima.ms.scope.enums.ServiceTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class ScopeServiceTest {

    @Resource
    private ScopeService scopeService;

    @Test
    void saveOrUpdate() {
        List<Point> pointList = Arrays.asList(new Point(116.340064,40.061245),
                new Point(116.347081,40.061836),
                new Point(116.34751,40.05842),
                new Point(116.342446,40.058092),
                new Point(116.340064,40.061245));
        Boolean result = this.scopeService.saveOrUpdate(2L, ServiceTypeEnum.ORGAN, new GeoJsonPolygon(pointList));
        System.out.println(result);
    }

    @Test
    void delete() {
    }

    @Test
    void testDelete() {
    }

    @Test
    void queryById() {
    }

    @Test
    void queryByBidAndType() {
    }

    @Test
    void queryListByPoint() {
    }

    @Test
    void testQueryListByPoint() {
        GeoJsonPoint point = new GeoJsonPoint(116.344828,40.05911);
        List<ServiceScopeEntity> serviceScopeEntities = this.scopeService.queryListByPoint(ServiceTypeEnum.ORGAN, point);
        serviceScopeEntities.forEach(serviceScopeEntity -> System.out.println(serviceScopeEntity));
    }

    @Test
    void testQueryListByPoint2() {
        String address = "北京市昌平区金燕龙办公楼";
        List<ServiceScopeEntity> serviceScopeEntities = this.scopeService.queryListByPoint(ServiceTypeEnum.ORGAN, address);
        serviceScopeEntities.forEach(serviceScopeEntity -> System.out.println(serviceScopeEntity));
    }

    @Test
    void queryAdjacentScopes() {
        // 测试查询相邻网点功能
        Double longitude = 116.344828;
        Double latitude = 40.05911;

        // 查询3个相邻网点
        List<ServiceScopeEntity> adjacentScopes = this.scopeService.queryAdjacentScopes(
            longitude, latitude, ServiceTypeEnum.ORGAN, 3);

        System.out.println("查询到的相邻网点数量: " + adjacentScopes.size());
        adjacentScopes.forEach(serviceScopeEntity -> {
            System.out.println("网点ID: " + serviceScopeEntity.getBid() +
                             ", 类型: " + serviceScopeEntity.getType());
        });
    }

    @Test
    void queryAdjacentScopesWithoutLimit() {
        // 测试查询相邻网点功能，不限制数量（使用默认值）
        Double longitude = 116.344828;
        Double latitude = 40.05911;

        List<ServiceScopeEntity> adjacentScopes = this.scopeService.queryAdjacentScopes(
            longitude, latitude, ServiceTypeEnum.ORGAN, null);

        System.out.println("查询到的相邻网点数量（无限制）: " + adjacentScopes.size());
        adjacentScopes.forEach(serviceScopeEntity -> {
            System.out.println("网点ID: " + serviceScopeEntity.getBid() +
                             ", 类型: " + serviceScopeEntity.getType());
        });
    }
}
