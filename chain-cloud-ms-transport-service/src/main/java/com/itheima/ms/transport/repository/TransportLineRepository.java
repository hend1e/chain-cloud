package com.itheima.ms.transport.repository;

import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.transport.domain.TransportLineNodeDTO;
import com.itheima.chaincloud.domain.transport.domain.TransportLineSearchDTO;
import com.itheima.ms.transport.entity.line.TransportLine;
import com.itheima.ms.transport.entity.node.AgencyEntity;
import com.itheima.ms.transport.entity.node.BaseEntity;

import java.util.List;

/**
 * 运输路线查询
 *
 * @author zzj
 * @version 1.0
 */
public interface TransportLineRepository {

    TransportLineNodeDTO findShortestPath(AgencyEntity start, AgencyEntity end);

    TransportLineNodeDTO findShortestPath(AgencyEntity start, AgencyEntity end, int depth);

    List<TransportLineNodeDTO> findPathList(AgencyEntity start, AgencyEntity end, int depth, int limit);

    Long queryCount(BaseEntity firstNode, BaseEntity secondNode);

    Long create(BaseEntity firstNode, BaseEntity secondNode, TransportLine transportLine);

    Long update(TransportLine transportLine);

    Long remove(Long lineId);

    PageResponse<TransportLine> queryPageList(TransportLineSearchDTO transportLineSearchDTO);

    List<TransportLine> queryByIds(Long... ids);

    TransportLine queryById(Long id);
}
