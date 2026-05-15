package com.itheima.ms.transport.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.api.aigc.AIGCFeign;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.common.util.StrUtil;
import com.itheima.chaincloud.domain.aigc.domain.CostFactorResponse;
import com.itheima.chaincloud.domain.transport.domain.DispatchConfigurationDTO;
import com.itheima.chaincloud.domain.transport.domain.OrganDTO;
import com.itheima.chaincloud.domain.transport.domain.TransportLineNodeDTO;
import com.itheima.chaincloud.domain.transport.domain.TransportLineSearchDTO;
import com.itheima.chaincloud.domain.transport.enums.DispatchMethodEnum;
import com.itheima.em.sdk.EagleMapTemplate;
import com.itheima.em.sdk.enums.ProviderEnum;
import com.itheima.em.sdk.vo.Coordinate;
import com.itheima.ms.transport.entity.line.TransportLine;
import com.itheima.ms.transport.entity.node.AgencyEntity;
import com.itheima.ms.transport.entity.node.BaseEntity;
import com.itheima.ms.transport.entity.node.OLTEntity;
import com.itheima.ms.transport.entity.node.TLTEntity;
import com.itheima.ms.transport.enums.ExceptionEnum;
import com.itheima.ms.transport.enums.TransportLineEnum;
import com.itheima.ms.transport.repository.TransportLineRepository;
import com.itheima.ms.transport.service.CostConfigurationService;
import com.itheima.ms.transport.service.DispatchConfigurationService;
import com.itheima.ms.transport.service.OrganService;
import com.itheima.ms.transport.service.TransportLineService;
import com.itheima.ms.transport.utils.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransportLineServiceImpl implements TransportLineService {

    private final TransportLineRepository transportLineRepository;
    private final EagleMapTemplate eagleMapTemplate;
    private final OrganService organService;
    private final CostConfigurationService costConfigurationService;
    private final DispatchConfigurationService dispatchConfigurationService;
    private final AIGCFeign aigcFeign;

    @Override
    public Boolean createLine(TransportLine transportLine) {
        var transportLineEnum = TransportLineEnum.codeOf(transportLine.getType());
        if (null == transportLineEnum) {
            throw new CCException(ExceptionEnum.TRANSPORT_LINE_TYPE_ERROR);
        }

        if (ObjectUtil.equal(transportLine.getStartOrganId(), transportLine.getEndOrganId())) {
            throw new CCException(ExceptionEnum.TRANSPORT_LINE_ORGAN_CANNOT_SAME);
        }

        BaseEntity firstNode;
        BaseEntity secondNode;
        switch (transportLineEnum) {
            case TRUNK_LINE: {
                firstNode = OLTEntity.builder().bid(transportLine.getStartOrganId()).build();
                secondNode = OLTEntity.builder().bid(transportLine.getEndOrganId()).build();
                break;
            }
            case BRANCH_LINE: {
                firstNode = TLTEntity.builder().bid(transportLine.getStartOrganId()).build();
                secondNode = OLTEntity.builder().bid(transportLine.getEndOrganId()).build();
                break;
            }
            case CONNECT_LINE: {
                firstNode = AgencyEntity.builder().bid(transportLine.getStartOrganId()).build();
                secondNode = TLTEntity.builder().bid(transportLine.getEndOrganId()).build();
                break;
            }
            default: {
                throw new CCException(ExceptionEnum.TRANSPORT_LINE_TYPE_ERROR);
            }
        }

        if (ObjectUtil.hasEmpty(firstNode, secondNode)) {
            throw new CCException(ExceptionEnum.START_END_ORGAN_NOT_FOUND);
        }

        var count = this.transportLineRepository.queryCount(firstNode, secondNode);
        if (count > 0) {
            throw new CCException(ExceptionEnum.TRANSPORT_LINE_ALREADY_EXISTS);
        }

        transportLine.setId(null);
        transportLine.setCreated(System.currentTimeMillis());
        transportLine.setUpdated(transportLine.getCreated());
        this.infoFromMap(firstNode, secondNode, transportLine);

        count = this.transportLineRepository.create(firstNode, secondNode, transportLine);
        return count > 0;
    }

    private void infoFromMap(BaseEntity firstNode, BaseEntity secondNode, TransportLine transportLine) {
        var startOrgan = this.organService.findByBid(firstNode.getBid());
        if (ObjectUtil.hasEmpty(startOrgan, startOrgan.getLongitude(), startOrgan.getLatitude())) {
            throw new CCException("请先完善机构信息");
        }
        var endOrgan = this.organService.findByBid(secondNode.getBid());
        if (ObjectUtil.hasEmpty(endOrgan, endOrgan.getLongitude(), endOrgan.getLatitude())) {
            throw new CCException("请先完善机构信息");
        }

        var origin = new Coordinate(startOrgan.getLongitude(), startOrgan.getLatitude());
        var destination = new Coordinate(endOrgan.getLongitude(), endOrgan.getLatitude());
        var param = Map.<String, Object>of("show_fields", "cost");
        var driving = this.eagleMapTemplate.opsForDirection().driving(ProviderEnum.AMAP, origin, destination, param);
        if (StrUtil.isEmpty(driving)) {
            return;
        }
        var jsonObject = JSONUtil.parseObj(driving);
        var duration = Convert.toLong(jsonObject.getByPath("route.paths[0].cost.duration"), -1L);
        transportLine.setTime(duration);
        var distance = Convert.toDouble(jsonObject.getByPath("route.paths[0].distance"), -1d);
        transportLine.setDistance(NumberUtil.round(distance, 0).doubleValue());

        var costConfig = costConfigurationService.findCostByType(transportLine.getType());
        var distanceKm = distance / 1000.0;
        var baseCost = distanceKm * costConfig;

        CostFactorResponse costFactor = null;
        try {
            String routeInfo = this.buildRouteInfo(startOrgan, endOrgan, transportLine, distanceKm, baseCost, costConfig);
            costFactor = this.aigcFeign.identifyCostFactor(routeInfo);
            transportLine.setExtra(JSONUtil.toJsonStr(costFactor));
        } catch (Exception e) {
            System.err.println("AIGC cost factor identification failed: " + e.getMessage());
        }

        var finalCost = this.calculateFinalCost(baseCost, costFactor);
        transportLine.setCost(NumberUtil.round(finalCost, 2).doubleValue());
    }

    @Override
    public Boolean updateLine(TransportLine transportLine) {
        TransportLine transportLineData = this.queryById(transportLine.getId());
        if (null == transportLineData) {
            throw new CCException(ExceptionEnum.TRANSPORT_LINE_NOT_FOUND);
        }
        BeanUtil.copyProperties(transportLine, transportLineData, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("type", "startOrganId", "startOrganName", "endOrganId", "endOrganName"));
        transportLineData.setUpdated(System.currentTimeMillis());
        Long count = this.transportLineRepository.update(transportLineData);
        return count > 0;
    }

    @Override
    public Boolean deleteLine(Long id) {
        Long count = this.transportLineRepository.remove(id);
        return count > 0;
    }

    @Override
    public PageResponse<TransportLine> queryPageList(TransportLineSearchDTO transportLineSearchDTO) {
        return this.transportLineRepository.queryPageList(transportLineSearchDTO);
    }

    @Override
    public TransportLineNodeDTO queryShortestPath(Long startId, Long endId) {
        AgencyEntity start = AgencyEntity.builder().bid(startId).build();
        AgencyEntity end = AgencyEntity.builder().bid(endId).build();
        if (cn.hutool.core.util.ObjectUtil.hasEmpty(start, end)) {
            throw new CCException(ExceptionEnum.START_END_ORGAN_NOT_FOUND);
        }
        return this.transportLineRepository.findShortestPath(start, end);
    }

    @Override
    public TransportLineNodeDTO findLowestPath(Long startId, Long endId) {
        AgencyEntity start = AgencyEntity.builder().bid(startId).build();
        AgencyEntity end = AgencyEntity.builder().bid(endId).build();
        if (cn.hutool.core.util.ObjectUtil.hasEmpty(start, end)) {
            throw new CCException(ExceptionEnum.START_END_ORGAN_NOT_FOUND);
        }
        List<TransportLineNodeDTO> pathList = this.transportLineRepository.findPathList(start, end, 10, 1);
        if (CollUtil.isNotEmpty(pathList)) {
            return pathList.get(0);
        }
        return null;
    }

    @Override
    public TransportLineNodeDTO queryPathByDispatchMethod(Long startId, Long endId) {
        DispatchConfigurationDTO configuration = this.dispatchConfigurationService.findConfiguration();
        int method = configuration.getDispatchMethod();
        if (ObjectUtil.equal(DispatchMethodEnum.SHORTEST_PATH.getCode(), method)) {
            return this.queryShortestPath(startId, endId);
        } else {
            return this.findLowestPath(startId, endId);
        }
    }

    @Override
    public List<TransportLine> queryByIds(Long... ids) {
        return this.transportLineRepository.queryByIds(ids);
    }

    @Override
    public TransportLine queryById(Long id) {
        return this.transportLineRepository.queryById(id);
    }

    private String buildRouteInfo(OrganDTO startOrgan, OrganDTO endOrgan, TransportLine transportLine,
                                  Double distanceKm, Double baseCost, Double unitCostByType) {
        var startOrganAddress = JSONUtil.parseObj(startOrgan.getAddress());
        var endOrganAddress = JSONUtil.parseObj(endOrgan.getAddress());
        var sameCity = ObjectUtil.equal(startOrganAddress.getByPath("city.id", Long.class), endOrganAddress.getByPath("city.id", Long.class));
        var sameProvince = ObjectUtil.equal(startOrganAddress.getByPath("province.id", Long.class), endOrganAddress.getByPath("province.id", Long.class));
        String routeInfoTemplate = ResourceUtil.readStr("templates/aigc-cost-factor.txt", CharsetUtil.CHARSET_UTF_8);
        var params = MapUtil.builder()
                .put("lineType", TransportLineEnum.codeOf(transportLine.getType()).name())
                .put("startOrganId", startOrgan.getId())
                .put("endOrganId", endOrgan.getId())
                .put("startOrganName", startOrgan.getName())
                .put("endOrganName", endOrgan.getName())
                .put("startOrganLevel", startOrgan.getType())
                .put("endOrganLevel", endOrgan.getType())
                .put("startCoordinate", StrUtil.format("{},{}", startOrgan.getLongitude(), startOrgan.getLatitude()))
                .put("endCoordinate", StrUtil.format("{},{}", endOrgan.getLongitude(), endOrgan.getLatitude()))
                .put("unitCostByType", unitCostByType)
                .put("baseCost", baseCost)
                .put("sameCity", sameCity)
                .put("sameProvince", sameProvince)
                .put("crossProvince", !sameProvince)
                .put("distanceKm", distanceKm)
                .put("durationHour", NumberUtil.div(transportLine.getTime().doubleValue(), 3600d, 2))
                .put("straightLineDistanceKm", DistanceCalculator.calculateDistance(startOrgan.getLongitude(), startOrgan.getLatitude(), endOrgan.getLongitude(), endOrgan.getLatitude()))
                .build();
        return StrUtil.format(routeInfoTemplate, params);
    }

    private Double calculateFinalCost(Double baseCost, CostFactorResponse costFactor) {
        if (costFactor == null) {
            return baseCost;
        }
        if (costFactor.getConfidence() != null && costFactor.getConfidence() < 0.60) {
            return baseCost;
        }
        double regionFactor = getValidFactor(costFactor.getRegionFactor());
        double distanceSegmentFactor = getValidFactor(costFactor.getDistanceSegmentFactor());
        double efficiencyFactor = getValidFactor(costFactor.getEfficiencyFactor());
        double tollFactor = getValidFactor(costFactor.getTollFactor());
        double detourFactor = getValidFactor(costFactor.getDetourFactor());
        double routeComplexityFactor = getValidFactor(costFactor.getRouteComplexityFactor());
        double corridorFactor = getValidFactor(costFactor.getCorridorFactor());
        double similarityCalibrationFactor = getValidFactor(costFactor.getSimilarityCalibrationFactor());
        double accessFactor = getValidFactor(costFactor.getAccessFactor());
        double totalFactor = regionFactor * distanceSegmentFactor * efficiencyFactor * tollFactor *
                detourFactor * routeComplexityFactor * corridorFactor *
                similarityCalibrationFactor * accessFactor;
        if (totalFactor < 0.85 || totalFactor > 1.25) {
            return baseCost;
        }
        return baseCost * totalFactor;
    }

    private double getValidFactor(Double factor) {
        if (factor == null || factor <= 0) {
            return 1.00;
        }
        return factor;
    }

}
