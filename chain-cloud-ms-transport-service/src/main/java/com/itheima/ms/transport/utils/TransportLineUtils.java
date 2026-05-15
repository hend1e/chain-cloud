package com.itheima.ms.transport.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import com.itheima.chaincloud.domain.transport.domain.OrganDTO;
import com.itheima.chaincloud.domain.transport.domain.TransportLineDTO;
import com.itheima.chaincloud.domain.transport.domain.TransportLineNodeDTO;
import com.itheima.ms.transport.entity.line.TransportLine;
import com.itheima.ms.transport.enums.OrganTypeEnum;
import org.neo4j.driver.internal.value.PathValue;
import org.neo4j.driver.types.Path;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对象转化工具类
 *
 * @author zzj
 * @version 1.0
 */
public class TransportLineUtils {

    public static TransportLine toEntity(TransportLineDTO transportLineDTO) {
        return BeanUtil.toBeanIgnoreError(transportLineDTO, TransportLine.class);
    }

    public static TransportLineDTO toDTO(TransportLine transportLine) {
        return BeanUtil.toBeanIgnoreError(transportLine, TransportLineDTO.class);
    }

    public static List<TransportLineDTO> toDTOList(List<TransportLine> transportLine) {
        return transportLine.stream()
                .map(TransportLineUtils::toDTO)
                .collect(Collectors.toList());
    }

    public static TransportLineNodeDTO convert(PathValue pathValue) {
        TransportLineNodeDTO transportLineNodeDTO = new TransportLineNodeDTO();
        Path path = pathValue.asPath();

        path.nodes().forEach(node -> {
            Map<String, Object> map = node.asMap();
            OrganDTO organDTO = BeanUtil.toBeanIgnoreError(map, OrganDTO.class);
            organDTO.setType(OrganTypeEnum.valueOf(CollUtil.getFirst(node.labels())).getCode());
            organDTO.setLatitude(BeanUtil.getProperty(map.get("location"), "y"));
            organDTO.setLongitude(BeanUtil.getProperty(map.get("location"), "x"));
            transportLineNodeDTO.getNodeList().add(organDTO);
        });

        path.relationships().forEach(relationship -> {
            Map<String, Object> objectMap = relationship.asMap();
            double cost = Convert.toDouble(objectMap.get("cost"), 0d);
            transportLineNodeDTO.setCost(NumberUtil.add(cost, transportLineNodeDTO.getCost().doubleValue()));
        });

        transportLineNodeDTO.setCost(NumberUtil.round(transportLineNodeDTO.getCost(), 2).doubleValue());
        return transportLineNodeDTO;
    }
}
