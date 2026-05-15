package com.itheima.ms.web.manager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.itheima.chaincloud.api.base.trips.TripsFeign;
import com.itheima.chaincloud.domain.base.truck.TruckTripsDTO;
import com.itheima.chaincloud.api.transport.CostConfigurationFeign;
import com.itheima.chaincloud.api.transport.DispatchConfigurationFeign;
import com.itheima.chaincloud.api.transport.TransportLineFeign;
import com.itheima.ms.web.manager.service.TransportService;
import com.itheima.ms.web.manager.vo.transportLine.TransportLineQueryVO;
import com.itheima.ms.web.manager.vo.transportLine.TransportLineVO;
import com.itheima.ms.web.manager.vo.work.CostConfigurationVO;
import com.itheima.ms.web.manager.vo.work.DispatchConfigurationVO;
import com.itheima.chaincloud.common.exception.CCWebException;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.transport.domain.CostConfigurationDTO;
import com.itheima.chaincloud.domain.transport.domain.DispatchConfigurationDTO;
import com.itheima.chaincloud.domain.transport.domain.TransportLineDTO;
import com.itheima.chaincloud.domain.transport.domain.TransportLineSearchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 线路服务
 * 对应线路微服务
 */
@Slf4j
@Service
public class TransportServiceImpl implements TransportService {

    @Resource
    TransportLineFeign transportLineFeign;

    @Resource
    private TripsFeign tripsFeign;

    @Resource
    private DispatchConfigurationFeign dispatchConfigurationFeign;

    @Resource
    private CostConfigurationFeign costConfigurationFeign;

    /**
     * 新增线路
     *
     * @param vo 线路信息
     */
    @Override
    public void saveTransportLine(TransportLineVO vo) {
        TransportLineDTO dto = BeanUtil.toBean(vo, TransportLineDTO.class);
        transportLineFeign.createLine(dto);
    }

    /**
     * 更新线路
     *
     * @param id 线路ID
     * @param vo 线路信息
     */
    @Override
    public void updateTransportLine(Long id, TransportLineVO vo) {
        TransportLineDTO dto = BeanUtil.toBean(vo, TransportLineDTO.class);
        dto.setId(id);
        dto.setDistance(ObjectUtil.isNotEmpty(dto.getDistance()) ? dto.getDistance() * 1000 : 0L);
        transportLineFeign.updateLine(dto);
    }

    /**
     * 线路分页
     *
     * @param transportLineQueryVO 查询条件
     * @return 线路列表
     */
    @Override
    public PageResponse<TransportLineVO> findTransportLineByPage(TransportLineQueryVO transportLineQueryVO) {
        PageResponse<TransportLineDTO> transportLineDtoPage = transportLineFeign.queryPageList(BeanUtil.toBean(transportLineQueryVO, TransportLineSearchDTO.class));
        //加工数据
        return PageResponse.of(transportLineDtoPage, this::parseTransportLineDto2VO);
    }

    /**
     * 线路详情
     *
     * @param id 线路ID
     * @return 线路信息
     */
    @Override
    public TransportLineVO findTransportLineById(Long id) {
        TransportLineDTO dto = transportLineFeign.queryById(id);
        if (ObjectUtil.isEmpty(dto)) {
            return new TransportLineVO();
        }
        return parseTransportLineDto2VO(dto);
    }

    /**
     * 线路详情
     *
     * @param ids 线路IDs
     * @return 线路信息列表
     */
    @Override
    public List<TransportLineVO> findTransportLineByIds(List<Long> ids) {
        List<TransportLineDTO> transportLineDTOS = transportLineFeign.queryByIds(ids.toArray(Long[]::new));
        return parseTransportLineDto2VO(transportLineDTOS);
    }

    /**
     * 删除线路
     * 检查是否有车次
     *
     * @param id 线路ID
     */
    @Override
    public void deleteTransportLine(Long id) {
        // 判断线路
        var tripsDTOS = tripsFeign.findAll(id, null);
        if (ObjectUtil.isNotEmpty(tripsDTOS)) {
            throw new CCWebException("请先删除线路下车次");
        }
        transportLineFeign.deleteLine(id);
    }

    /**
     * 线路转换
     *
     * @param transportLineDtoList 线路DTO
     * @return 线路VO
     */
    private List<TransportLineVO> parseTransportLineDto2VO(List<TransportLineDTO> transportLineDtoList) {
        return transportLineDtoList.stream().map(dto -> {
            dto.setDistance(ObjectUtil.isNotEmpty(dto.getDistance()) ? dto.getDistance() / 1000 : 0L);
            dto.setTime(ObjectUtil.isNotEmpty(dto.getTime()) ? dto.getTime() / 60 : 0L);
            return BeanUtil.toBean(dto, TransportLineVO.class);
        }).collect(Collectors.toList());
    }

    /**
     * 线路转换
     *
     * @param dto 线路DTO
     * @return 线路VO
     */
    private TransportLineVO parseTransportLineDto2VO(TransportLineDTO dto) {
        dto.setDistance(ObjectUtil.isNotEmpty(dto.getDistance()) ? dto.getDistance() / 1000 : 0L);
        dto.setTime(ObjectUtil.isNotEmpty(dto.getTime()) ? dto.getTime() / 60 : 0L);
        return BeanUtil.toBean(dto, TransportLineVO.class);
    }

    /**
     * 保存调度配置
     * @param vo 调度配置
     */
    @Override
    public void saveConfiguration(DispatchConfigurationVO vo) {
        DispatchConfigurationDTO dispatchConfigurationDTO = BeanUtil.toBean(vo, DispatchConfigurationDTO.class);
        dispatchConfigurationFeign.saveConfiguration(dispatchConfigurationDTO);
    }

    /**
     * 查询调度配置
     * @return 调度配置
     */
    @Override
    public DispatchConfigurationVO findConfiguration() {
        DispatchConfigurationDTO configuration = dispatchConfigurationFeign.findConfiguration();
        return BeanUtil.toBean(configuration, DispatchConfigurationVO.class);
    }

    /**
     * 保存成本配置
     * @param vo 配置信息
     */
    @Override
    public void saveCostConfiguration(List<CostConfigurationVO> vo) {
        List<CostConfigurationDTO> costConfigurationDTO = BeanUtil.copyToList(vo, CostConfigurationDTO.class);
        costConfigurationFeign.saveConfiguration(costConfigurationDTO);
    }

    /**
     * 查询成本配置
     * @return 配置信息
     */
    @Override
    public List<CostConfigurationVO> findCostConfiguration() {
        List<CostConfigurationDTO> configuration = costConfigurationFeign.findConfiguration();
        return BeanUtil.copyToList(configuration, CostConfigurationVO.class);
    }
}
