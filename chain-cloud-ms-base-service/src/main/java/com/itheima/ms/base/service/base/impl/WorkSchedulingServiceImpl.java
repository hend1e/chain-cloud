package com.itheima.ms.base.service.base.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SimpleQuery;
import com.google.common.collect.Lists;
import com.itheima.chaincloud.domain.base.basic.WorkSchedulingAddDTO;
import com.itheima.chaincloud.domain.base.basic.WorkSchedulingDTO;
import com.itheima.chaincloud.domain.base.basic.WorkSchedulingQueryDTO;
import com.itheima.chaincloud.domain.base.basic.WorkSchedulingUpdateDTO;
import com.itheima.chaincloud.domain.base.enums.WorkPatternEnum;
import com.itheima.chaincloud.domain.base.enums.WorkStatusEnum;
import com.itheima.chaincloud.domain.base.enums.WorkUserTypeEnum;
import com.itheima.ms.base.entity.base.WorkHistorySchedulingEntity;
import com.itheima.ms.base.entity.base.WorkPatternEntity;
import com.itheima.ms.base.entity.base.WorkSchedulingEntity;
import com.itheima.ms.base.entity.user.TruckDriverEntity;
import com.itheima.ms.base.mapper.base.WorkHistorySchedulingMapper;
import com.itheima.ms.base.mapper.base.WorkSchedulingMapper;
import com.itheima.ms.base.service.base.WorkPatternService;
import com.itheima.ms.base.service.base.WorkSchedulingService;
import com.itheima.ms.base.service.user.TruckDriverService;
import com.itheima.ms.base.utils.WorkSchedulingUtils;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.BeanUtil;
import com.itheima.chaincloud.common.util.DateUtils;
import com.itheima.chaincloud.common.util.PageResponse;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排班服务
 */
@Service
public class WorkSchedulingServiceImpl extends ServiceImpl<WorkSchedulingMapper, WorkSchedulingEntity> implements WorkSchedulingService {

    @Resource
    private WorkHistorySchedulingMapper workHistorySchedulingMapper;

    @Resource
    private WorkPatternService workPatternService;

    @Resource
    private TruckDriverService truckDriverService;

    /**
     * 分页查询排班
     *
     * @param workSchedulingQueryDTO 查询条件
     * @return 排班
     */
    @Override
    public PageResponse<WorkSchedulingDTO> queryForPage(WorkSchedulingQueryDTO workSchedulingQueryDTO) {

        final String queryMonth = StringUtils.isEmpty(workSchedulingQueryDTO.getMonth()) ?
                LocalDateTimeUtil.format(LocalDateTimeUtil.now(), DateUtils.DEFAULT_MONTH_FORMAT) :
                workSchedulingQueryDTO.getMonth();

        boolean isQueryHistory = WorkSchedulingUtils.isQueryHistory(queryMonth);

        LambdaQueryWrapper<WorkSchedulingEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper
                .eq(WorkSchedulingEntity::getIsDelete, 0)
                .eq(ObjectUtil.isNotEmpty(workSchedulingQueryDTO.getUserType()), WorkSchedulingEntity::getUserType, workSchedulingQueryDTO.getUserType())
                .like(ObjectUtil.isNotEmpty(workSchedulingQueryDTO.getName()), WorkSchedulingEntity::getName, workSchedulingQueryDTO.getName())
                .like(ObjectUtil.isNotEmpty(workSchedulingQueryDTO.getEmployeeNumber()), WorkSchedulingEntity::getEmployeeNumber, workSchedulingQueryDTO.getEmployeeNumber())
                .like(ObjectUtil.isNotEmpty(workSchedulingQueryDTO.getAgencyId()), WorkSchedulingEntity::getAgencyId, workSchedulingQueryDTO.getAgencyId())
                .eq(ObjectUtil.isNotEmpty(workSchedulingQueryDTO.getWorkPatternId()), WorkSchedulingEntity::getWorkPatternId, workSchedulingQueryDTO.getWorkPatternId())
                .orderByDesc(WorkSchedulingEntity::getCreated);
        Page<WorkSchedulingEntity> entityPage = getBaseMapper().selectPage
                (new Page<>(workSchedulingQueryDTO.getPage(),
                        workSchedulingQueryDTO.getPageSize()), queryWrapper);

        int dayNumOfQueryMonth = DateUtils.getMonthEndTime(queryMonth).getDayOfMonth();
        final Map<String, String> historyMap = new HashMap<>();

        List<WorkSchedulingEntity> records = entityPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageResponse.of(entityPage, WorkSchedulingDTO.class);
        }

        if (isQueryHistory) {
            List<Long> userIdList = records.stream().map(WorkSchedulingEntity::getUserId).collect(Collectors.toList());
            List<WorkHistorySchedulingEntity> historyEntity = workHistorySchedulingMapper.selectList(
                    Wrappers.<WorkHistorySchedulingEntity>lambdaQuery()
                            .eq(WorkHistorySchedulingEntity::getWorkMonth, queryMonth)
                            .in(WorkHistorySchedulingEntity::getUserId, userIdList)
                            .between(WorkHistorySchedulingEntity::getWorkDay, 0, dayNumOfQueryMonth)
            );
            if (CollUtil.isNotEmpty(historyEntity)) {
                historyEntity.parallelStream().forEach(entity -> historyMap.put(entity.getUserId() + "" + entity.getWorkDay(), ""));
            }
        }

        Map<Long, WorkPatternEntity> patternByIds = getPatternByIds(records);

        return PageResponse.of(entityPage, WorkSchedulingDTO.class, (entity, dto) -> {
            List<Boolean> workSchedules = new ArrayList<>();
            for (int count = 1; count <= dayNumOfQueryMonth; count++) {
                workSchedules.add(WorkSchedulingUtils.isWorded(entity, queryMonth, count, patternByIds.get(entity.getWorkPatternId()), historyMap, false));
            }
            dto.setWorkSchedules(workSchedules);
            appenWorkPatten(dto, patternByIds);
        });
    }

    /**
     * 新增排班
     *
     * @param workSchedulingAddDTO 排班
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(WorkSchedulingAddDTO workSchedulingAddDTO) {
        WorkSchedulingEntity workSchedulingEntity = BeanUtil.toBean(workSchedulingAddDTO, WorkSchedulingEntity.class, (dto, entity) -> {
            entity.setId(IdWorker.getId());
            entity.setCreater(dto.getOperator());
            entity.setUpdater(dto.getOperator());
            entity.setCreated(LocalDateTime.now());
            entity.setUpdated(LocalDateTime.now());
        });
        int insert = getBaseMapper().insert(workSchedulingEntity);
        if (insert <= 0) {
            throw new CCException("新增工作品排班失败");
        }
    }

    /**
     * 删除排班
     *
     * @param id       排班ID
     * @param operator 操作人
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long operator) {
        WorkSchedulingEntity entity = new WorkSchedulingEntity();
        entity.setId(id);
        entity.setIsDelete(true);
        entity.setUpdater(operator);
        if (getBaseMapper().updateById(entity) <= 0) {
            throw new CCException("删除操作失败");
        }
    }

    /**
     * 批量新增排班
     *
     * @param workSchedulingAddDTOList 排班列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(List<WorkSchedulingAddDTO> workSchedulingAddDTOList) {

        if (CollUtil.isEmpty(workSchedulingAddDTOList)) {
            return;
        }
        List<WorkSchedulingEntity> batchAddEntityList = new ArrayList<>();

        for (int count = 0; count < workSchedulingAddDTOList.size(); count++) {
            WorkSchedulingEntity workSchedulingEntity = BeanUtil.toBean(workSchedulingAddDTOList.get(count), WorkSchedulingEntity.class, (dto, entity) -> {
                entity.setId(IdWorker.getId());
                entity.setCreater(dto.getOperator());
                entity.setUpdater(dto.getOperator());
                entity.setCreated(LocalDateTime.now());
                entity.setUpdated(LocalDateTime.now());
                entity.setUserId(Long.parseLong(entity.getEmployeeNumber()));
                entity.setState(WorkStatusEnum.NOMAL.getStatus());
                if (dto.getWorkPatternType() == WorkPatternEnum.CONTINUITYS.getType()) {
                    entity.setWorkContinueStartTime(DateUtils.getStartTime(LocalDateTime.now()));
                }
                BeanUtil.setDefault(entity);
            });
            batchAddEntityList.add(workSchedulingEntity);
            if (batchAddEntityList.size() % 500 == 0 || count == workSchedulingAddDTOList.size() - 1) {
                getBaseMapper().batchInsert(batchAddEntityList);
            }
        }
        workSchedulingAddDTOList.parallelStream().forEach(v -> {
            if (ObjectUtil.notEqual(v.getUserType(), WorkUserTypeEnum.DRIVER.getCode().byteValue())) {
                return;
            }
            TruckDriverEntity one = truckDriverService.findOne(v.getUserId());
            if (ObjectUtil.isEmpty(one)) {
                TruckDriverEntity truckDriverEntity = new TruckDriverEntity();
                truckDriverEntity.setUserId(v.getUserId());
                truckDriverService.save(truckDriverEntity);
            }
        });

    }

    /**
     * 更新排班
     *
     * @param workSchedulingUpdateDTO 排班
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WorkSchedulingUpdateDTO workSchedulingUpdateDTO) {

        WorkSchedulingMapper workSchedulingMapper = getBaseMapper();
        WorkSchedulingEntity entityInDb = workSchedulingMapper.selectById(workSchedulingUpdateDTO.getId());
        if (entityInDb == null) {
            throw new CCException("无法进行更新");
        }

        WorkSchedulingEntity workSchedulingEntity = BeanUtil.toBean(workSchedulingUpdateDTO, WorkSchedulingEntity.class, (dto, entity) -> {
            entity.setUpdated(LocalDateTime.now());
            entity.setUpdater(dto.getOperator());
            WorkPatternEntity workPatternEntity = workPatternService.getById(entityInDb.getWorkPatternId());
            if (workSchedulingUpdateDTO.getWorkPatternType() == WorkPatternEnum.CONTINUITYS.getType()
                    && (ObjectUtil.isEmpty(workPatternEntity) || workPatternEntity.getWorkPatternType() != WorkPatternEnum.CONTINUITYS.getType())) {
                entity.setWorkContinueStartTime(DateUtils.getStartTime(LocalDateTime.now()));
            }
        });
        int result = workSchedulingMapper.updateById(workSchedulingEntity);
        if (result <= 0) {
            throw new CCException("更新操作失败");
        }
    }

    /**
     * 根据用户id查询这个月排班计划
     *
     * @param userId 用户ID
     * @return 未来一周排班计划
     */
    @Override
    public WorkSchedulingDTO currentSchedule(Long userId) {

        List<WorkSchedulingEntity> entities = getBaseMapper().
                selectByMap(Map.of("user_id", userId, "is_delete", 0));
        List<WorkSchedulingDTO> workSchedulingDTOS = getWorkSchedulingDTOS(entities, null);
        if (CollUtil.isEmpty(workSchedulingDTOS)) {
            return null;
        }
        return workSchedulingDTOS.get(0);
    }

    /**
     * 根据用户id查询
     *
     * @param userId 用户ID
     * @return 排班数据
     */
    @Override
    public WorkSchedulingEntity getByUserId(Long userId) {
        List<WorkSchedulingEntity> entities = getBaseMapper().
                selectByMap(Map.of("user_id", userId, "is_delete", 0));
        if (CollUtil.isNotEmpty(entities)) {
            return entities.get(0);
        }
        return null;
    }

    /**
     * 根据网点id查询该网点所有员工的排班信息
     *
     * @param agencyId 机构ID
     * @return 今天工作人员排班
     */
    @Override
    public List<WorkSchedulingDTO> monthScheduleByAgencyId(Long agencyId) {
        List<WorkSchedulingEntity> entities = getBaseMapper().
                selectByMap(Map.of("agency_id", agencyId, "is_delete", 0));
        return getWorkSchedulingDTOS(entities, null);
    }

    /**
     * 补充工作模式
     *
     * @param entities 排班信息
     * @param time     时间
     * @return 排班信息
     */
    @Nullable
    private List<WorkSchedulingDTO> getWorkSchedulingDTOS(List<WorkSchedulingEntity> entities, LocalDateTime time) {
        Map<Long, WorkPatternEntity> patternByIds = getPatternByIds(entities);
        if (CollUtil.isNotEmpty(entities)) {
            return entities.stream().map(entity -> {
                WorkSchedulingDTO parse = parse(entity, time);
                return appenWorkPatten(parse, patternByIds);
            }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 根据快递员/司机id列表或网点id查询当前工作排班
     *
     * @param userIds  用户ID
     * @param agencyId 机构ID
     * @param type     用户类型：1:员工，2：快递员，3：司机
     * @param time     时间
     * @return 当前工作排班
     */
    @Override
    public List<WorkSchedulingDTO> monthSchedule(List<Long> userIds, Long agencyId, Byte type, LocalDateTime time) {
        if (CollUtil.isEmpty(userIds) && agencyId == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<WorkSchedulingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .in(CollUtil.isNotEmpty(userIds), WorkSchedulingEntity::getUserId, userIds)
                .eq(ObjectUtil.isNotEmpty(agencyId), WorkSchedulingEntity::getAgencyId, agencyId)
                .eq(WorkSchedulingEntity::getUserType, type);
        List<WorkSchedulingEntity> entities = getBaseMapper().selectList(queryWrapper);
        return getWorkSchedulingDTOS(entities, time);

    }

    /**
     * 补充工作模式信息
     *
     * @param workSchedulingDTO    dto
     * @param workPatternEntityMap 工作模型map
     * @return 工作模式
     */
    private WorkSchedulingDTO appenWorkPatten(WorkSchedulingDTO workSchedulingDTO, Map<Long, WorkPatternEntity> workPatternEntityMap) {
        if (CollUtil.isNotEmpty(workPatternEntityMap)) {
            WorkPatternEntity workPatternEntity = workPatternEntityMap.get(workSchedulingDTO.getWorkPatternId());
            if (ObjectUtil.isNotEmpty(workPatternEntity)) {
                workSchedulingDTO.setWorkStartMinute1(workPatternEntity.getWorkStartMinute1());
                workSchedulingDTO.setWorkEndMinute1(workPatternEntity.getWorkEndMinute1());
                workSchedulingDTO.setWorkPatternName(workPatternEntity.getName());
            }
        }
        return workSchedulingDTO;
    }


    /**
     * 结构转换
     *
     * @param entity 数据实体
     * @param time   未来的某个时间
     * @return 工作模式dto
     */
    private WorkSchedulingDTO parse(WorkSchedulingEntity entity, LocalDateTime time) {
        Map<Long, WorkPatternEntity> patternByIds = getPatternByIds(Lists.newArrayList(entity));
        return BeanUtil.toBean(entity, WorkSchedulingDTO.class, (workSchedulingEntity, workSchedulingDTO) -> {
            if (workSchedulingEntity.getWorkPatternId() == 0) {
                workSchedulingDTO.setWorkSchedules(null);
            } else {
                LocalDateTime startTime = ObjectUtil.isEmpty(time) ? LocalDateTime.now() : time;
                int currentDay = startTime.getDayOfMonth();
                int monthNumber = DateUtils.getMonthNumber(startTime);
                List<Boolean> workList = new ArrayList<>();
                for (int count = currentDay; count <= monthNumber; count++) {
                    boolean worded = WorkSchedulingUtils.isWorded(entity, LocalDateTimeUtil.format(startTime, DateUtils.DEFAULT_MONTH_FORMAT),
                            count, patternByIds.get(workSchedulingEntity.getWorkPatternId()), null, false);
                    workList.add(worded);
                }
                workSchedulingDTO.setWorkSchedules(workList);
            }
        });

    }

    /**
     * 获取工作模式信息
     *
     * @param records 排班
     * @return 工作模式
     */
    private Map<Long, WorkPatternEntity> getPatternByIds(List<WorkSchedulingEntity> records) {
        if (CollUtil.isEmpty(records)) {
            return new HashMap<>();
        }
        List<Long> workPatternIds = records.stream().map(WorkSchedulingEntity::getWorkPatternId).collect(Collectors.toList());
        return SimpleQuery.keyMap(
                Wrappers.<WorkPatternEntity>lambdaQuery().in(WorkPatternEntity::getId, workPatternIds),
                WorkPatternEntity::getId);

    }

    /**
     * 获取整个计划（运输任务）期间每一天都上班的司机
     *
     * @param driverIds         司机ID列表
     * @param planDepartureTime 计划发车时间
     * @param planArrivalTime   计划到达时间
     * @return 正常上班的司机ID列表
     */
    @Override
    public List<Long> getWorkingDrivers(List<Long> driverIds, LocalDateTime planDepartureTime, LocalDateTime planArrivalTime) {

        LambdaQueryWrapper<WorkSchedulingEntity> queryWrapper = Wrappers.<WorkSchedulingEntity>lambdaQuery()
                .in(WorkSchedulingEntity::getUserId, driverIds)
                .eq(WorkSchedulingEntity::getUserType, WorkUserTypeEnum.DRIVER.getCode().byteValue());
        List<WorkSchedulingEntity> list = list(queryWrapper);
        Map<Long, WorkPatternEntity> patternByIds = getPatternByIds(list);
        List<Long> workUserIds = list.stream().filter(workSchedulingEntity -> {
            for (LocalDateTime count = planDepartureTime; count.isBefore(planArrivalTime); count = count.plusDays(1)) {
                int dayOfMonth = count.getDayOfMonth();
                String month = LocalDateTimeUtil.format(count, DateUtils.DEFAULT_MONTH_FORMAT);
                boolean worded = WorkSchedulingUtils.isWorded(workSchedulingEntity, month, dayOfMonth, patternByIds.get(workSchedulingEntity.getWorkPatternId()), null, false);
                if (!worded) {
                    return false;
                }
            }
            return true;
        }).map(WorkSchedulingEntity::getUserId).collect(Collectors.toList());

        if (workUserIds.size() <= 2) {
            return workUserIds;
        }
        return workUserIds.subList(0, 2);
    }
}