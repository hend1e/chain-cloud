package com.itheima.ms.transport.repository;

import com.itheima.ms.transport.entity.node.BaseEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * Repository基本方法的实现
 *
 * @author zzj
 * @version 1.0
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, ID> extends Neo4jRepository<T, ID> {

    Optional<T> findByBid(Long bid);

    Long deleteByBid(Long bid);

}
