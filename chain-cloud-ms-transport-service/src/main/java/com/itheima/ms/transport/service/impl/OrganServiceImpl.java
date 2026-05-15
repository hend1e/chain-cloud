package com.itheima.ms.transport.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.domain.transport.domain.OrganDTO;
import com.itheima.ms.transport.enums.ExceptionEnum;
import com.itheima.ms.transport.repository.OrganRepository;
import com.itheima.ms.transport.service.OrganService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class OrganServiceImpl implements OrganService {
    @Resource
    private OrganRepository organRepository;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public OrganDTO findByBid(Long bid) {
        OrganDTO organDTO = this.organRepository.findByBid(bid);
        if (ObjectUtil.isNotEmpty(organDTO)) {
            return organDTO;
        }
        throw new CCException(ExceptionEnum.ORGAN_NOT_FOUND);
    }

    @Override
    public List<OrganDTO> findByBids(List<Long> bids) {
        List<OrganDTO> organDTOS = this.organRepository.findByBids(bids);
        if (ObjectUtil.isNotEmpty(organDTOS)) {
            return organDTOS;
        }
        throw new CCException(ExceptionEnum.ORGAN_NOT_FOUND);
    }

    @Override
    public List<OrganDTO> findAll(String name) {
        return this.organRepository.findAll(name);
    }

    @Override
    public String findAllTree() {
        List<OrganDTO> organList = this.findAll(null);
        if (CollUtil.isEmpty(organList)) {
            return "";
        }
        List<Tree<Long>> treeNodes = TreeUtil.build(organList, 0L,
                (organDTO, tree) -> {
                    tree.setId(organDTO.getId());
                    tree.setParentId(organDTO.getParentId());
                    tree.putAll(BeanUtil.beanToMap(organDTO));
                    tree.remove("bid");
                });
        try {
            return this.objectMapper.writeValueAsString(treeNodes);
        } catch (JsonProcessingException e) {
            throw new CCException("序列化json出错！", e);
        }
    }
}
