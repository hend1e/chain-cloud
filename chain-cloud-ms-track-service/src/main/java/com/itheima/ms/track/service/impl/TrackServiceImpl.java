package com.itheima.ms.track.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.api.oms.OrderFeign;
import com.itheima.chaincloud.api.transport.OrganFeign;
import com.itheima.chaincloud.api.work.TransportOrderFeign;
import com.itheima.chaincloud.api.work.TransportTaskFeign;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.CoordinateUtil;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.domain.oms.domain.OrderLocationDTO;
import com.itheima.chaincloud.domain.track.enums.TrackStatusEnum;
import com.itheima.chaincloud.domain.track.enums.TrackTypeEnum;
import com.itheima.chaincloud.domain.transport.domain.OrganDTO;
import com.itheima.chaincloud.domain.work.domain.TransportOrderDTO;
import com.itheima.em.sdk.EagleMapTemplate;
import com.itheima.em.sdk.enums.ProviderEnum;
import com.itheima.em.sdk.vo.Coordinate;
import com.itheima.ms.track.entity.TrackEntity;
import com.itheima.ms.track.enums.TrackExceptionEnum;
import com.itheima.ms.track.service.TrackService;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TrackServiceImpl implements TrackService {

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private EagleMapTemplate eagleMapTemplate;
    @Resource
    private TransportOrderFeign transportOrderFeign;
    @Resource
    private TransportTaskFeign transportTaskFeign;
    @Resource
    private OrderFeign orderFeign;
    @Resource
    private OrganFeign organFeign;

    @Override
    public boolean create(String transportOrderId) {
        Query query = Query.query(Criteria.where("transportOrderId").is(transportOrderId));
        TrackEntity trackEntity = this.mongoTemplate.findOne(query, TrackEntity.class);
        if (ObjectUtil.isNotEmpty(trackEntity)) {
            throw new CCException(TrackExceptionEnum.TRACK_ALREADY_EXISTS);
        }

        TrackEntity track = new TrackEntity();
        track.setTransportOrderId(transportOrderId);
        track.setCreated(System.currentTimeMillis());
        track.setUpdated(track.getCreated());
        track.setStatus(TrackStatusEnum.NEW);
        //路线规划
        track.setPlanGeoJsonLine(this.queryPlanGeoJsonLineString(transportOrderId, track));

        // 保存
        this.mongoTemplate.save(track);
        return true;
    }

    /**
     * 查询路线规划，将运转节点作为途经点进行规划（高德地图最多支持16个途经点）
     *
     * @param transportOrderId 运单号
     * @return 轨迹点数据
     */
    private GeoJsonLineString queryPlanGeoJsonLineString(String transportOrderId, TrackEntity track) {
        //通过运单号查询运单，将转运节点作为途经点
        TransportOrderDTO transportOrderDTO = this.transportOrderFeign.findById(transportOrderId);
        String transportLine = transportOrderDTO.getTransportLine();

        String wayPoints = null; //途经点，分号分割
        if (StrUtil.isNotEmpty(transportLine)) {
            JSONObject transportLineJson = JSONUtil.parseObj(transportLine);
            JSONArray nodeList = transportLineJson.getJSONArray("nodeList");

            List<String> pointList = nodeList.stream().map(obj -> {
                        JSONObject json = (JSONObject) obj;
                        double longitude = json.getDouble("longitude", 0d);
                        double latitude = json.getDouble("latitude", 0d);
                        if (ObjectUtil.equalsAny(0d, longitude, latitude)) {
                            return "err";
                        }
                        return StrUtil.format("{},{}", longitude, latitude);
                    }).filter(o -> ObjectUtil.notEqual(o, "err"))
                    .collect(Collectors.toList());
            wayPoints = StrUtil.join(";", pointList);
        } else {
            //收件人与发件人在同一个网点，没有转运路线，将当前机构id作为途经点
            OrganDTO organDTO = this.organFeign.queryById(transportOrderDTO.getCurrentAgencyId());
            if (ObjectUtil.isNotEmpty(organDTO)) {
                wayPoints = StrUtil.format("{},{}", organDTO.getLongitude(), organDTO.getLatitude());
            } else {
                //理论上没有这种情况，如果出现这种情况就不设置途经点了
            }
        }

        //查询订单位置数据
        OrderLocationDTO orderLocationDTO = this.orderFeign.findOrderLocationByOrderId(transportOrderDTO.getOrderId());
        CoordinateUtil.Coordinate origin = CoordinateUtil.format(orderLocationDTO.getSendLocation());
        CoordinateUtil.Coordinate destination = CoordinateUtil.format(orderLocationDTO.getReceiveLocation());

        //设置参数
        Map<String, Object> param = MapUtil.<String, Object>builder()
                .put(ObjectUtil.isNotEmpty(wayPoints), "waypoints", wayPoints) //途经点
                .put("show_fields", "polyline") //返回
                .build();
        //驾车路线规划
        String driving = this.eagleMapTemplate.opsForDirection()
                .driving(ProviderEnum.AMAP, new Coordinate(origin), new Coordinate(destination), param);

        if (StrUtil.isEmpty(driving)) {
            return null;
        }
        JSONObject jsonObject = JSONUtil.parseObj(driving);
        //距离，单位：米
        Double distance = Convert.toDouble(jsonObject.getByPath("route.paths[0].distance"), -1d);
        track.setDistance(distance);

        JSONArray steps = jsonObject.getByPath("route.paths[0].steps", JSONArray.class);
        //将所有的坐标点收集起来
        List<Point> points = new ArrayList<>();
        List<Object> polyLines = CollUtil.getFieldValues(steps, "polyline");
        for (Object polyLine : polyLines) {
            List<GeoJsonPoint> list = StrUtil.split(Convert.toStr(polyLine), ';')
                    .stream().map(coordinateStr -> {
                        double[] ds = Convert.convert(double[].class, StrUtil.splitTrim(coordinateStr, ','));
                        return new GeoJsonPoint(ds[0], ds[1]);
                    }).collect(Collectors.toList());
            points.addAll(list);
        }

        //TODO 如果路线规则的节点比较多，可能会出现内存溢出的情况，学生思考下解决方案

        return new GeoJsonLineString(points);

    }

    @Override
    public boolean complete(List<String> transportOrderIds) {
        //更新的条件
        Query query = Query.query(Criteria.where("transportOrderId").in(transportOrderIds));
        //更新的数据
        Update update = Update.update("status", TrackStatusEnum.COMPLETE);
        UpdateResult updateResult = this.mongoTemplate.updateMulti(query, update, TrackEntity.class);
        return updateResult.getModifiedCount() > 0;
    }

    @Override
    public TrackEntity queryByTransportOrderId(String transportOrderId) {
        Query query = Query.query(Criteria.where("transportOrderId").is(transportOrderId));
        return this.mongoTemplate.findOne(query, TrackEntity.class);
    }

    @Override
    public boolean uploadFromTruck(Long transportTaskId, double lng, double lat) {
        //通过运输任务查询订单号列表
        List<String> list = this.transportTaskFeign.queryTransportOrderIdListById(transportTaskId);

        //位置上报
        return upload(list, lng, lat, TrackTypeEnum.DRIVER);
    }

    @Override
    public boolean uploadFromCourier(List<String> transportOrderIds, double lng, double lat) {
        //位置上报
        return upload(transportOrderIds, lng, lat, TrackTypeEnum.COURIER);
    }

    /**
     * 位置上报
     *
     * @param transportOrderIds 运单ids
     * @param lng               经度
     * @param lat               纬度
     * @param trackTypeEnum   轨迹状态
     * @return 是否成功
     */
    private boolean upload(List<String> transportOrderIds, double lng, double lat, TrackTypeEnum trackTypeEnum) {
        if (CollUtil.isEmpty(transportOrderIds)) {
            return false;
        }

        //更新的条件
        Query query = Query.query(Criteria.where("transportOrderId")
                .in(transportOrderIds)
                .and("status").is(TrackStatusEnum.NEW));

        //更新的数据
        Update update = Update.update("lastPoint", new GeoJsonPoint(lng, lat))
                .set("type", trackTypeEnum)
                .set("updated", System.currentTimeMillis());
        UpdateResult updateResult = this.mongoTemplate.updateMulti(query, update, TrackEntity.class);
        return updateResult.getModifiedCount() > 0;
    }
}
