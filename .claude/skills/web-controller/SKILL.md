---
name: web-controller
description: 定义项目中 Web 模块 Controller 编写规范，用于面向前端的 REST 接口实现
---

## 编写规范

### 1. 文件命名
- 文件名以 `Controller` 结尾
- 示例：`TruckController.java`、`OrderController.java`、`TaskController.java`

### 2. 类命名
- 类名与文件名保持一致
- 使用大驼峰命名，以 `Controller` 结尾

### 3. 包结构规范
- 包名：`com.itheima.ms.web.<模块名>.controller[.<子域>]`
- 模块名：`manager`（管理端）、`customer`（客户端）、`courier`（快递员端）、`driver`（司机端）
- 示例：
  - `com.itheima.ms.web.manager.controller.base.truck.TruckController`
  - `com.itheima.ms.web.customer.controller.OrderController`
  - `com.itheima.ms.web.courier.controller.TaskController`

### 4. 必需注解

#### 4.1 类级注解
```java
@RestController
@Api(tags = "车辆管理")
@Slf4j
public class TruckController {
```

| 注解 | 必需 | 说明 |
|------|------|------|
| `@RestController` | 是 | REST 控制器 |
| `@Api` | 推荐 | Swagger 分组标签 |
| `@Slf4j` | 推荐 | 日志记录 |
| `@RequestMapping` | 可选 | 基础路径（可在方法级定义） |
| `@Validated` | 可选 | 启用参数校验 |

#### 4.2 方法级注解
```java
@ApiOperation(value = "获取车辆分页数据")
@ApiImplicitParams({
        @ApiImplicitParam(name = "page", value = "页码", required = true, example = "1"),
        @ApiImplicitParam(name = "pageSize", value = "页尺寸", required = true, example = "10")
})
@GetMapping("/truck/page")
public R<PageResponse<TruckVO>> findTruckByPage(...) {
```

| 注解 | 说明 |
|------|------|
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` | HTTP 方法 |
| `@ApiOperation` | 方法描述（必需） |
| `@ApiImplicitParams` | 参数描述（路径参数、查询参数） |

### 5. 关键特征：返回 `R<VO>` 包装

**Web Controller 必须使用 `R<>` 包装返回值：**

```java
// 正确：使用 R<> 包装 VO
@GetMapping("/truck/{id}")
public R<TruckVO> findTruckById(@PathVariable(name = "id") Long id) {
    return R.success(truckService.findTruckDetail(id));
}

// 正确：无返回数据时
@PutMapping("/disable/{id}")
public R<Void> disable(@PathVariable(name = "id") Long id) {
    truckService.disableTruck(id);
    return R.success();
}

// 错误：不要直接返回 VO
@GetMapping("/truck/{id}")
public TruckVO findTruckById(@PathVariable Long id) {  // 错误！缺少 R<> 包装
    return truckService.findTruckDetail(id);
}
```

### 6. 使用 VO 而非 DTO

**Web Controller 接收和返回 VO 对象：**

```java
// 正确：使用 VO
@PostMapping("/truck")
public R<TruckVO> saveTruck(@RequestBody TruckVO vo) {
    truckService.saveTruck(vo);
    return R.success();
}

// 错误：不要使用 DTO
@PostMapping("/truck")
public R<TruckDTO> saveTruck(@RequestBody TruckDTO dto) {  // 错误！应使用 VO
    // ...
}
```

### 7. 委托给 Service 层

**Web Controller 不直接调用 Feign，而是委托给 Web Service：**

```java
// 正确：委托给 Web Service
@GetMapping("/truck/page")
public R<PageResponse<TruckVO>> findTruckByPage(...) {
    return R.success(truckService.findTruckByPage(page, pageSize, ...));
}

// 错误：直接调用 Feign
@GetMapping("/truck/page")
public R<PageResponse<TruckVO>> findTruckByPage(...) {
    PageResponse<TruckDTO> result = truckFeign.findByPage(...);  // 错误！应通过 Service
    // ...
}
```

### 8. `R<>` 响应封装

**成功响应：**
```java
// 有数据
return R.success(data);

// 无数据
return R.success();
```

**错误响应：**
```java
// 简单错误消息
return R.error("操作失败");

// 带数据的错误
return R.error(data, "身份校验未通过");
```

**`R<>` 结构：**
```java
{
    "code": 200,      // 200-成功，非200-失败
    "msg": "ok",      // 提示消息
    "data": { ... }   // 响应数据
}
```

### 9. 分页查询

**返回 `R<PageResponse<VO>>`：**

```java
@GetMapping("/truck/page")
public R<PageResponse<TruckVO>> findTruckByPage(
        @RequestParam(name = "page") Integer page,
        @RequestParam(name = "pageSize") Integer pageSize,
        @RequestParam(name = "truckTypeId", required = false) Long truckTypeId) {
    return R.success(truckService.findTruckByPage(page, pageSize, truckTypeId));
}
```

### 10. 参数校验

**配合 `@Validated` 和 VO 中的校验注解：**

```java
@RestController
@Validated  // 类级启用
public class OrderController {

    @PostMapping
    public R<OrderVO> mailingSave(@RequestBody @Valid MailingSaveVO vo) {
        // VO 中的 @NotNull、@Min 等注解会自动校验
        return R.success(orderService.mailingSave(vo));
    }
}
```

### 11. 依赖注入

**注入 Web Service（不是 Feign）：**

```java
@Resource
private BaseTruckService baseTruckService;  // Web Service

// 错误：不要直接注入 Feign
@Resource
private TruckFeign truckFeign;  // 错误！应通过 Web Service 间接调用
```

### 12. 导入语句

```java
// 通用响应
import com.itheima.chaincloud.common.vo.R;
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

### 13. 完整示例

```java
package com.itheima.ms.web.manager.controller.base.truck;

import com.itheima.ms.web.manager.service.BaseTruckService;
import com.itheima.ms.web.manager.vo.baseTruck.*;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.common.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Api(tags = "车辆管理")
@Slf4j
public class TruckController {

    @Resource
    private BaseTruckService baseTruckService;

    @ApiOperation(value = "添加车辆")
    @PostMapping("/truck")
    public R<TruckVO> saveTruck(@RequestBody TruckVO vo) {
        baseTruckService.saveTruck(vo);
        return R.success();
    }

    @ApiOperation(value = "获取车辆详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "车辆id", required = true, example = "1")
    })
    @GetMapping("/truck/{id}")
    public R<TruckVO> findTruckById(@PathVariable(name = "id") Long id) {
        return R.success(baseTruckService.findTruckDetail(id));
    }

    @ApiOperation(value = "获取车辆分页数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true, example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "页尺寸", required = true, example = "10")
    })
    @GetMapping("/truck/page")
    public R<PageResponse<TruckVO>> findTruckByPage(
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "pageSize") Integer pageSize,
            @RequestParam(name = "truckTypeId", required = false) Long truckTypeId) {
        return R.success(baseTruckService.findTruckByPage(page, pageSize, truckTypeId));
    }

    @ApiOperation(value = "停用车辆")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "车辆id", required = true, example = "1")
    })
    @PutMapping("/disable/{id}")
    public R<Void> disable(@PathVariable(name = "id") Long id) {
        baseTruckService.disableTruck(id);
        return R.success();
    }
}
```

**错误示例：**
```java
// 错误1：缺少 R<> 包装
@GetMapping("/truck/{id}")
public TruckVO findTruckById(@PathVariable Long id) {  // 错误：缺少 R<>
    return truckService.findTruckDetail(id);
}

// 错误2：使用 DTO 而非 VO
@GetMapping("/truck/{id}")
public R<TruckDTO> findTruckById(@PathVariable Long id) {  // 错误：应使用 TruckVO
    return R.success(truckService.findTruckDetail(id));
}

// 错误3：直接调用 Feign
@GetMapping("/truck/{id}")
public R<TruckVO> findTruckById(@PathVariable Long id) {
    TruckDTO dto = truckFeign.findById(id);  // 错误：应通过 Service
    return R.success(BeanUtil.toBean(dto, TruckVO.class));
}

// 错误4：缺少 @ApiOperation
@GetMapping("/truck/{id}")
public R<TruckVO> findTruckById(@PathVariable Long id) {  // 缺少 @ApiOperation
    return R.success(truckService.findTruckDetail(id));
}
```

### 14. Web Controller 与 Service Controller 的区别

| 特性 | Web Controller | Service Controller |
|------|---------------|-------------------|
| 位置 | `chain-cloud-ms-web-*` | `chain-cloud-ms-*-service` |
| 返回类型 | `R<VO>` | 原始 `DTO` |
| 数据来源 | 调用 Web Service | 调用本地 Service |
| 对象类型 | VO（View Object） | DTO（Data Transfer Object） |
| 用途 | 提供前端 API | 提供 Feign 接口实现 |
| 异常处理 | `CCWebException` → HTTP 200 | `CCException` → HTTP 错误码 |

### 15. 注意事项

- **必须使用 `R<>` 包装**：前端依赖统一的响应格式
- **使用 VO 而非 DTO**：VO 面向前端，可能包含额外展示字段
- **委托给 Web Service**：不直接调用 Feign，保持 Controller 简洁
- **`@ApiOperation` 必需**：前端依赖 Swagger 文档
- **异常由 `GlobalExceptionHandler` 处理**：Controller 不捕获异常
- **路径可灵活定义**：不必与 Feign 的 `path` 对应
