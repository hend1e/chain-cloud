package com.itheima.ms.base.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.base.basic.WorkPatternAddDTO;
import com.itheima.chaincloud.domain.base.basic.WorkPatternDTO;
import com.itheima.chaincloud.domain.base.basic.WorkPatternQueryDTO;
import com.itheima.chaincloud.domain.base.basic.WorkPatternUpdateDTO;
import com.itheima.ms.base.entity.base.WorkPatternEntity;

import java.util.List;

/**
 * 工作模式服务
 */
public interface WorkPatternService extends IService<WorkPatternEntity> {

    /**
     * 分页查询工作模式
     * @param workPatternQueryDTO 查询条件
     * @return 工作模式数据
     */
    PageResponse<WorkPatternDTO> page(WorkPatternQueryDTO workPatternQueryDTO);

    /**
     * 工作模式ID查询
     * @param id 工作模式ID
     * @return 工作模式
     */
    WorkPatternDTO findById(Long id);

    /**
     * 删除工作模式
     * @param id 工作模式ID
     */
    void delete(long id);

    /**
     * 更新工作模式
     * @param workPatternUpdateDTO 工作模式
     */
    void update(WorkPatternUpdateDTO workPatternUpdateDTO);

    /**
     * 新增工作模式
     * @param workPatternAddDTO 工作模式
     */
    void add(WorkPatternAddDTO workPatternAddDTO);

    List<WorkPatternDTO> all();
}