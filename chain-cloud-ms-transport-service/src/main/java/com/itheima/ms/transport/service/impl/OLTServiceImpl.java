package com.itheima.ms.transport.service.impl;

import com.itheima.ms.transport.entity.node.OLTEntity;
import com.itheima.ms.transport.repository.OLTRepository;
import com.itheima.ms.transport.service.OLTService;
import org.springframework.stereotype.Service;

@Service
public class OLTServiceImpl extends ServiceImpl<OLTRepository, OLTEntity>
        implements OLTService {
}
