---
name: service-controller
description: 定义项目中业务微服务 Controller 编写规范，用于内部 Feign 接口的 REST 实现
---

## 编写规范

### 1. 文件命名
- 文件名以 `Controller` 结尾
- 示例：`AreaController.java`、`TruckController.java`、`OrderController.java`

### 2. 类命名
- 类名与文件名保持一致
- 使用大驼峰命名，以 `Controller` 结尾

### 3. 包结构规范
- 包名：`com.itheima.ms.<服务名>.controller[.<子域>]`
- 子域按业务分组：`base/`、`truck/`、`user/` 等
- 示例：
  - `com.itheima.ms.base.controller.base.AreaController`
  - `com.itheima.ms.base.controller.truck.TruckController`
  - `com.itheima.ms.oms.controller.OrderController`

### 4. 必需注解

#### 4.1 类级注解
```java
@Slf4j
@RestController
@RequestMapping("/area")
@Api(value = "Area", tags = "行政区域")
public class AreaController {
```

| 注解 | 必需 | 说明 |
|------|------|------|
| `@RestController` | 是 | REST 控制器 |
| `@RequestMapping` | 是 | 基础路径，与 Feign 的 `path` 对应 |
| `@Api` | 推荐 | Swagger 分组标签 |
| `@Slf4j` | 推荐 | 日志记录 |
| `@Validated` | 可选 | 启用参数校验 |

#### 4.2 方法级注解
```java
@GetMapping("/{id}")
@ApiOperation(value = "根据 id 获取详情")
@ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "id", required = true, dataTypeClass = Long.class)
})
public AreaDTO get(@PathVariable Long id) {
```

| 注解 | 说明 |
|------|------|
| `@GetMapping` / `@PostMapping` / `@PutMapping` | HTTP 方法 |
| `@ApiOperation` | 方法描述 |
| `@ApiImplicitParams` | 参数描述（路径参数、查询参数） |

### 5. 关键特征：返回原始 DTO

**Service Controller 直接返回 DTO，不使用 `R<>` 包装：**

```java
// 正确：返回原始 DTO
@GetMapping("/{id}")
public AreaDTO get(@PathVariable Long id) {
    return BeanUtil.toBean(areaService.getById(id), AreaDTO.class);
}

// 错误：不要使用 R<> 包装
@GetMapping("/{id}")
public R<AreaDTO> get(@PathVariable Long id) {  // 错误！
    return R.success(areaService.getById(id));
}
```

> **原因**：Service Controller 是 Feign 接口的实现，Feign 期望直接获取 DTO。`R<>` 包装仅在 Web Controller 中使用。

### 6. RESTful 路由约定

| 操作 | HTTP 方法 | 路径 | 示例 |
|------|----------|------|------|
| 根据 ID 查询 | `GET` | `/{id}` | `@GetMapping("/{id}")` |
| 分页查询 | `GET` 或 `POST` | `/page` | `@GetMapping("/page")` 或 `@PostMapping("/page")` |
| 列表查询 | `GET` 或 `POST` | `/list` 或 `/batch` | `@GetMapping("/batch")` |
| 新增 | `POST` | `/` | `@PostMapping` |
| 更新 | `PUT` | `/{id}` | `@PutMapping("/{id}")` |
| 删除 | `PUT` | `/{id}/del` | `@PutMapping("/{id}/del")` |
| 禁用 | `PUT` | `/{id}/disable` | `@PutMapping("/{id}/disable")` |
| 启用 | `PUT` | `/{id}/enable` | `@PutMapping("/{id}/enable")` |
| 统计 | `GET` | `/count` | `@GetMapping("/count")` |

### 7. Entity-DTO 转换

**在 Controller 层使用 `BeanUtil.toBean()` 进行转换：**

```java
// Entity -> DTO
@GetMapping("/{id}")
public TruckDTO fineById(@PathVariable(name = "id") Long id) {
    TruckEntity truckEntity = truckService.getById(id);
    if (ObjectUtil.isEmpty(truckEntity)) {
        return null;
    }
    return BeanUtil.toBean(truckEntity, TruckDTO.class);
}

// DTO -> Entity
@PostMapping
public TruckDTO saveTruck(@RequestBody TruckDTO dto) {
    TruckEntity truckEntity = BeanUtil.toBean(dto, TruckEntity.class);
    truckService.save(truckEntity);
    BeanUtil.copyProperties(truckEntity, dto);
    return dto;
}

// 批量转换
@GetMapping
public List<TruckDTO> findAll(@RequestParam(name = "ids", required = false) List<Long> ids) {
    return truckService.findAll(ids).stream()
            .map(entity -> BeanUtil.toBean(entity, TruckDTO.class))
            .collect(Collectors.toList());
}
```

### 8. 分页查询

**返回 `PageResponse<DTO>`：**

```java
@GetMapping("/page")
public PageResponse<TruckDTO> findByPage(@RequestParam(name = "page") Integer page,
                                         @RequestParam(name = "pageSize") Integer pageSize,
                                         @RequestParam(name = "truckTypeId", required = false) Long truckTypeId) {
    IPage<TruckEntity> pageResult = truckService.findByPage(page, pageSize, truckTypeId);
    
    List<TruckDTO> dtoList = pageResult.getRecords().stream()
            .map(entity -> BeanUtil.toBean(entity, TruckDTO.class))
            .collect(Collectors.toList());
    
    return PageResponse.<TruckDTO>builder()
            .items(dtoList)
            .pageSize(pageSize)
            .page(page)
            .counts(pageResult.getTotal())
            .pages(pageResult.getPages())
            .build();
}
```

**或使用 `PageResponse` 构造方法：**
```java
@PostMapping("page")
public PageResponse<TransportOrderDTO> findByPage(@RequestBody TransportOrderQueryDTO queryDTO) {
    Page<TransportOrderEntity> pageResult = transportOrderService.findByPage(queryDTO);
    return new PageResponse<>(pageResult, TransportOrderDTO.class);
}
```

### 9. 依赖注入

使用 `@Resource` 或 `@Autowired` 注入 Service：

```java
@Resource
private TruckService truckService;

@Autowired
private AreaService areaService;
```

### 10. 参数注解

#### 10.1 `@PathVariable`
```java
@GetMapping("/{id}")
public AreaDTO get(@PathVariable Long id) {
```

#### 10.2 `@RequestParam`
```java
@GetMapping("/page")
public PageResponse<TruckDTO> findByPage(
        @RequestParam(name = "page") Integer page,
        @RequestParam(name = "pageSize") Integer pageSize,
        @RequestParam(name = "truckTypeId", required = false) Long truckTypeId) {
```

#### 10.3 `@RequestBody`
```java
@PostMapping
public OrderDTO mailingSave(@RequestBody MailingSaveDTO mailingSaveDTO) {
```

### 11. 导入语句

```java
// Hutool
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;

// MyBatis-Plus 分页
import com.baomidou.mybatisplus.core.metadata.IPage;

// 通用分页响应
import com.itheima.chaincloud.common.util.PageResponse;

// Swagger
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

// Spring
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
```

### 12. 完整示例

```java
package com.itheima.ms.base.controller.truck;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.base.truck.TruckDTO;
import com.itheima.ms.base.entity.truck.TruckEntity;
import com.itheima.ms.base.service.truck.TruckService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("base/truck")
@Api(tags = "车辆管理")
public class TruckController {

    @Resource
    private TruckService truckService;

    @GetMapping("/{id}")
    @ApiOperation(value = "根据 id 获取车辆详情")
    public TruckDTO fineById(@PathVariable(name = "id") Long id) {
        TruckEntity truckEntity = truckService.getById(id);
        if (ObjectUtil.isEmpty(truckEntity)) {
            return null;
        }
        return BeanUtil.toBean(truckEntity, TruckDTO.class);
    }

    @GetMapping("/page")
    @ApiOperation(value = "获取车辆分页数据")
    public PageResponse<TruckDTO> findByPage(@RequestParam(name = "page") Integer page,
                                             @RequestParam(name = "pageSize") Integer pageSize,
                                             @RequestParam(name = "truckTypeId", required = false) Long truckTypeId) {
        IPage<TruckEntity> pageResult = truckService.findByPage(page, pageSize, truckTypeId);
        
        List<TruckDTO> dtoList = pageResult.getRecords().stream()
                .map(entity -> BeanUtil.toBean(entity, TruckDTO.class))
                .collect(Collectors.toList());
        
        return PageResponse.<TruckDTO>builder()
                .items(dtoList)
                .pageSize(pageSize)
                .page(page)
                .counts(pageResult.getTotal())
                .pages(pageResult.getPages())
                .build();
    }

    @PostMapping
    @ApiOperation(value = "添加车辆")
    public TruckDTO saveTruck(@RequestBody TruckDTO dto) {
        TruckEntity truckEntity = BeanUtil.toBean(dto, TruckEntity.class);
        truckService.save(truckEntity);
        BeanUtil.copyProperties(truckEntity, dto);
        return dto;
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新车辆信息")
    public TruckDTO update(@PathVariable(name = "id") Long id, @RequestBody TruckDTO dto) {
        dto.setId(id);
        TruckEntity truckEntity = BeanUtil.toBean(dto, TruckEntity.class);
        truckService.updateById(truckEntity);
        return dto;
    }

    @PutMapping("/{id}/disable")
    @ApiOperation(value = "禁用车辆")
    public void disable(@PathVariable(name = "id") Long id) {
        truckService.stopById(id);
    }

    @PutMapping("/{id}/enable")
    @ApiOperation(value = "启用车辆")
    public void enable(@PathVariable(name = "id") Long id) {
        truckService.workedById(id);
    }
}
```

**错误示例：**
```java
// 错误1：使用 R<> 包装返回值（这是 Web Controller 的做法）
@GetMapping("/{id}")
public R<TruckDTO> fineById(@PathVariable Long id) {  // 错误：不应使用 R<>
    return R.success(truckService.getById(id));
}

// 错误2：返回 Entity 而非 DTO
@GetMapping("/{id}")
public TruckEntity fineById(@PathVariable Long id) {  // 错误：应返回 TruckDTO
    return truckService.getById(id);
}

// 错误3：路径与 Feign 不匹配
// Feign 定义：@FeignClient(path = "/truck")
@RequestMapping("truck")  // 正确
// 或
@RequestMapping("/api/truck")  // 错误：路径不匹配

// 错误4：缺少 @ApiOperation
@GetMapping("/{id}")
public TruckDTO fineById(@PathVariable Long id) {  // 缺少 @ApiOperation
```

### 13. Service Controller 与 Web Controller 的区别

| 特性 | Service Controller | Web Controller |
|------|-------------------|----------------|
| 位置 | `chain-cloud-ms-*-service` | `chain-cloud-ms-web-*` |
| 返回类型 | 原始 `DTO` | `R<VO>` |
| 数据来源 | 直接调用本地 Service | 调用 Feign 接口 |
| 对象转换 | Entity ↔ DTO | DTO → VO |
| 用途 | 提供 Feign 接口实现 | 提供前端 API |

### 14. 注意事项

- **返回 DTO，不返回 Entity**：Entity 是内部对象，不应暴露给外部
- **不使用 `R<>` 包装**：Feign 期望直接获取 DTO
- **路径与 Feign `path` 对应**：确保 Feign 调用能正确路由
- **分页返回 `PageResponse<DTO>`**：统一的分页响应格式
- **转换在 Controller 层**：`BeanUtil.toBean(entity, XxxDTO.class)` 在 Controller 中完成
- **异常不捕获**：让异常自然抛出，由 `GlobalExceptionHandler` 统一处理
