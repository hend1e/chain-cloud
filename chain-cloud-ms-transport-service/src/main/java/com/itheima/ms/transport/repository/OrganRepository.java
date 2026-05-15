package com.itheima.ms.transport.repository;

import com.itheima.chaincloud.domain.transport.domain.OrganDTO;

import java.util.List;

/**
 * 通用机构查询
 *
 * @author zzj
 * @version 1.0
 */
public interface OrganRepository {

    OrganDTO findByBid(Long bid);

    List<OrganDTO> findByBids(List<Long> bids);

    List<OrganDTO> findAll(String name);
}
