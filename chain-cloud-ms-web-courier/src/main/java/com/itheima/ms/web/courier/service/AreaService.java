package com.itheima.ms.web.courier.service;

import com.itheima.ms.web.courier.vo.area.AreaSimpleVO;

import java.util.List;

public interface AreaService {
    List<AreaSimpleVO> findChildrenAreaByParentId(Long parentId);
}
