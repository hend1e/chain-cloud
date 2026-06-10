---
name: service-layer
description: 定义项目中 Service 接口与实现类编写规范，包括 MyBatis-Plus IService/ServiceImpl 继承模式、查询构建、分页和异常处理
---

## 编写规范

### 1. 文件命名

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| 接口 | `{业务名}Service.java` | `TruckService.java`、`OrderService.java` |
| 实现 | `{业务名}ServiceImpl.java` | `TruckServiceImpl.java`、`OrderServiceImpl.java` |

### 2. 包结构规范

```
com.itheima.ms.<服务名>.service[.<子域>]/
├── TruckService.java              -- 接口
└── impl/
    └── TruckServiceImpl.java      -- 实现
```

- 接口放在 `service/` 或 `service/<子域>/` 下
- 实现类放在 `service/impl/` 或 `service/<子域>/impl/` 下

### 3. Service 接口规范

#### 3.1 必须继承 `IService<Entity>`

```java
public interface TruckService extends IService<TruckEntity> {
    // 自定义业务方法
}
```

#### 3.2 方法命名规范

| 前缀 | 语义 | 示例 |
|------|------|------|
| `save*` / `create*` | 创建 | `saveOrder()`、`createLine()` |
| `findBy*` / `get*` | 查询 | `findByPage()`、`findById()`、`getByCode()` |
| `findAll*` | 批量查询 | `findAll(ids)`、`findAllTree()` |
| `update*` | 更新 | `update()`、`updateRunStatus()` |
| `del*` / `delete*` | 删除 | `del()`、`deleteLine()` |
| `disable*` / `enable*` | 启用禁用 | `disable()`、`enable()` |
| `count*` | 统计 | `countByType()`、`groupByStatus()` |
| `batch*` | 批量操作 | `batchSave()` |

#### 3.3 方法文档

每个方法必须添加 Javadoc 注释：

```java
/**
 * 获取车辆分页数据
 *
 * @param page         页码
 * @param pageSize     页尺寸
 * @param truckTypeId  车辆类型id
 * @return 车辆分页数据
 */
IPage<TruckEntity> findByPage(Integer page, Integer pageSize, Long truckTypeId);
```

### 4. ServiceImpl 实现类规范

#### 4.1 类声明

```java
@Slf4j
@Service
public class TruckServiceImpl extends ServiceImpl<TruckMapper, TruckEntity> implements TruckService {
    // 实现
}
```

| 注解 | 必需 | 说明 |
|------|------|------|
| `@Service` | 是 | Spring Bean 注册 |
| `@Slf4j` | 推荐 | 日志记录 |

#### 4.2 泛型参数

`ServiceImpl<Mapper, Entity>` 的泛型顺序：
1. 第一个泛型：对应的 Mapper 接口
2. 第二个泛型：对应的 Entity 类

```java
public class TruckServiceImpl extends ServiceImpl<TruckMapper, TruckEntity> implements TruckService
```

#### 4.3 依赖注入

使用 `@Resource` 注入其他 Service 和 Feign 客户端：

```java
@Resource
private TruckDriverService truckDriverService;

@Resource
private AreaFeign areaFeign;
```

### 5. 查询构建规范

#### 5.1 LambdaQueryWrapper（推荐）

使用 `LambdaQueryWrapper` 构建类型安全的查询条件：

```java
// 方式1：直接 new
LambdaQueryWrapper<TruckEntity> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(TruckEntity::getStatus, StatusEnum.NORMAL.getCode())
       .like(TruckEntity::getLicensePlate, licensePlate)
       .orderByDesc(TruckEntity::getCreated);

// 方式2：使用 Wrappers 工具（更简洁）
LambdaQueryWrapper<TruckEntity> wrapper = Wrappers.<TruckEntity>lambdaQuery()
    .eq(TruckEntity::getParentId, parentId);
```

#### 5.2 条件拼接

```java
LambdaQueryWrapper<TruckEntity> wrapper = new LambdaQueryWrapper<>();

// 字符串非空判断
if (StrUtil.isNotEmpty(licensePlate)) {
    wrapper.like(TruckEntity::getLicensePlate, licensePlate);
}

// 对象非空判断
if (ObjectUtil.isNotEmpty(truckTypeId)) {
    wrapper.eq(TruckEntity::getTruckTypeId, truckTypeId);
}

// 集合非空判断
if (CollUtil.isNotEmpty(ids)) {
    wrapper.in(TruckEntity::getId, ids);
}
```

#### 5.3 常用查询方法

| 方法 | SQL 效果 | 示例 |
|------|---------|------|
| `.eq()` | `=` | `.eq(Entity::getStatus, 1)` |
| `.ne()` | `!=` | `.ne(Entity::getStatus, 0)` |
| `.like()` | `LIKE '%xxx%'` | `.like(Entity::getName, "张")` |
| `.likeLeft()` | `LIKE '%xxx'` | `.likeLeft(Entity::getName, "三")` |
| `.likeRight()` | `LIKE 'xxx%'` | `.likeRight(Entity::getName, "张")` |
| `.in()` | `IN (...)` | `.in(Entity::getId, ids)` |
| `.between()` | `BETWEEN ? AND ?` | `.between(Entity::getCreated, start, end)` |
| `.orderByDesc()` | `ORDER BY xxx DESC` | `.orderByDesc(Entity::getCreated)` |
| `.last()` | 追加 SQL | `.last("limit 1")` |

### 6. 分页查询规范

```java
@Override
public IPage<TruckEntity> findByPage(Integer page, Integer pageSize, Long truckTypeId) {
    // 1. 创建分页对象
    Page<TruckEntity> iPage = new Page<>(page, pageSize);
    
    // 2. 构建查询条件
    LambdaQueryWrapper<TruckEntity> wrapper = new LambdaQueryWrapper<>();
    if (ObjectUtil.isNotEmpty(truckTypeId)) {
        wrapper.eq(TruckEntity::getTruckTypeId, truckTypeId);
    }
    wrapper.orderByDesc(TruckEntity::getCreated);
    
    // 3. 执行分页查询
    return super.page(iPage, wrapper);
}
```

**Controller 层分页结果转换：**

```java
IPage<TruckEntity> pageResult = truckService.findByPage(page, pageSize, truckTypeId);

// 转换为 DTO 分页响应
return PageResponse.<TruckDTO>builder()
    .items(dtoList)
    .pageSize(pageSize)
    .page(page)
    .counts(pageResult.getTotal())
    .pages(pageResult.getPages())
    .build();
```

### 7. Entity-DTO 转换规范

使用 Hutool 的 `BeanUtil.toBean()` 进行对象转换：

```java
// Entity -> DTO
TruckDTO dto = BeanUtil.toBean(truckEntity, TruckDTO.class);

// DTO -> Entity
TruckEntity entity = BeanUtil.toBean(dto, TruckEntity.class);

// 批量转换
List<TruckDTO> dtoList = entities.stream()
    .map(entity -> BeanUtil.toBean(entity, TruckDTO.class))
    .collect(Collectors.toList());
```

### 8. 异常处理规范

使用 `CCException` 抛出业务异常：

```java
// 简单消息
if (ObjectUtil.isEmpty(truckEntity)) {
    throw new CCException("车辆不存在");
}

// 带格式化参数
if (ObjectUtil.isEmpty(compute)) {
    throw new CCException(StrUtil.format("计算运费出错 {}", mailingSaveDTO));
}

// 带错误码（使用枚举）
throw new CCException(SomeExceptionEnum.VEHICLE_NOT_FOUND);
```

### 9. 常用 Service 方法

`IService<T>` 提供的常用方法（直接通过 `super` 或 `this` 调用）：

| 方法 | 说明 |
|------|------|
| `save(entity)` | 插入一条记录 |
| `saveBatch(entities)` | 批量插入 |
| `removeById(id)` | 根据 ID 删除 |
| `updateById(entity)` | 根据 ID 更新 |
| `getById(id)` | 根据 ID 查询 |
| `list(wrapper)` | 查询列表 |
| `page(page, wrapper)` | 分页查询 |
| `count(wrapper)` | 统计数量 |

### 10. 导入语句

#### Service 接口必需导入：
```java
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.ms.xxx.entity.XxxEntity;
```

#### ServiceImpl 必需导入：
```java
// MyBatis-Plus
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

// Hutool 工具
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

// 异常
import com.itheima.chaincloud.common.exception.CCException;

// Spring
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
```

### 11. 完整示例

**Service 接口：**
```java
package com.itheima.ms.base.service.truck;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.ms.base.entity.truck.TruckEntity;

import java.util.List;

/**
 * 车辆信息表 服务类
 */
public interface TruckService extends IService<TruckEntity> {

    /**
     * 获取车辆分页数据
     *
     * @param page        页码
     * @param pageSize    页尺寸
     * @param truckTypeId 车辆类型id
     * @return 车辆分页数据
     */
    IPage<TruckEntity> findByPage(Integer page, Integer pageSize, Long truckTypeId);

    /**
     * 获取车辆列表
     *
     * @param ids 车辆id列表
     * @return 车辆列表
     */
    List<TruckEntity> findAll(List<Long> ids);

    /**
     * 禁用车辆
     *
     * @param id 车辆id
     */
    void disable(Long id);
}
```

**ServiceImpl 实现：**
```java
package com.itheima.ms.base.service.truck.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.ms.base.entity.truck.TruckEntity;
import com.itheima.ms.base.mapper.truck.TruckMapper;
import com.itheima.ms.base.service.truck.TruckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 车辆信息表 服务实现类
 */
@Slf4j
@Service
public class TruckServiceImpl extends ServiceImpl<TruckMapper, TruckEntity> implements TruckService {

    /**
     * 获取车辆分页数据
     */
    @Override
    public IPage<TruckEntity> findByPage(Integer page, Integer pageSize, Long truckTypeId) {
        Page<TruckEntity> iPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<TruckEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (ObjectUtil.isNotEmpty(truckTypeId)) {
            wrapper.eq(TruckEntity::getTruckTypeId, truckTypeId);
        }
        wrapper.orderByDesc(TruckEntity::getCreated);
        
        return super.page(iPage, wrapper);
    }

    /**
     * 获取车辆列表
     */
    @Override
    public List<TruckEntity> findAll(List<Long> ids) {
        LambdaQueryWrapper<TruckEntity> wrapper = new LambdaQueryWrapper<>();
        if (CollUtil.isNotEmpty(ids)) {
            wrapper.in(TruckEntity::getId, ids);
        }
        wrapper.orderByDesc(TruckEntity::getCreated);
        return super.list(wrapper);
    }

    /**
     * 禁用车辆
     */
    @Override
    public void disable(Long id) {
        TruckEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new CCException("车辆不存在");
        }
        entity.setStatus(0);
        super.updateById(entity);
    }
}
```

**错误示例：**
```java
// 错误1：接口未继承 IService
public interface TruckService {  // 缺少 extends IService<TruckEntity>
}

// 错误2：实现类未继承 ServiceImpl
@Service
public class TruckServiceImpl implements TruckService {  // 缺少 extends ServiceImpl<TruckMapper, TruckEntity>
}

// 错误3：泛型顺序错误
public class TruckServiceImpl extends ServiceImpl<TruckEntity, TruckMapper>  // 错误：应为 <Mapper, Entity>

// 错误4：使用 @Autowired 而非 @Resource（项目约定用 @Resource）
@Autowired
private TruckDriverService truckDriverService;  // 推荐用 @Resource

// 错误5：手动拼接 SQL 字符串（应使用 LambdaQueryWrapper）
List<TruckEntity> list = baseMapper.selectList("SELECT * FROM cc_truck WHERE status = 1");  // 错误
```

### 12. 注意事项

- **不直接操作 Mapper**：通过 `baseMapper` 或继承的 `save()`/`getById()` 等方法操作数据库
- **分页统一返回 `IPage<T>`**：由 Controller 层转换为 `PageResponse<DTO>`
- **异常统一用 `CCException`**：不要用 `RuntimeException` 或其他异常
- **日志用 `@Slf4j`**：通过 `log.info()`、`log.error()` 记录关键操作
- **事务注解**：需要事务时在方法上添加 `@Transactional(rollbackFor = Exception.class)`
