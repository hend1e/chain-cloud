---
name: api-feign
description: 定义 项目 中 Feign API 接口编写规范，包括命名规范、注解要求和实现模式
---

## 编写规范

### 1. 文件命名
- 文件名必须以 `Feign` 结尾（全大写）
- 示例：`AreaFeign.java`、`TruckFeign.java`、`OrderFeign.java`

### 2. 类命名
- 类名与文件名保持一致
- 使用大驼峰命名，以 `Feign` 结尾
- 类名应准确描述业务领域

### 3. 包结构规范
- 包名：`com.itheima.chaincloud.api.<模块名>`
- 模块名与 `chain-cloud-ms-domain` 对齐：
  - `base/common/` — 基础通用（Area、Auth、Message、MQ、GoodsType、WorkPattern、WorkScheduling）
  - `base/truck/` — 车辆领域（Truck、TruckLicense、TruckPlan、TruckReturnRegister、TruckType）
  - `base/trips/` — 班次领域（Trips）
  - `base/user/` — 用户领域（Driver）
  - `oms/` — 订单管理（Order、Cargo、Good）
  - `transport/` — 运输（CostConfiguration、DispatchConfiguration、Organ、TransportLine）
- Feign 接口放在 `src/main/java/com/itheima/chaincloud/api/<模块名>/` 目录下

### 4. 必需注解

#### 4.1 `@FeignClient`
每个 Feign 接口必须添加 `@FeignClient` 注解：

```java
@FeignClient(name = "chain-cloud-ms-base", contextId = "Area", path = "/area")
```

| 属性 | 规范 | 示例 |
|------|------|------|
| `name` | 小写 kebab-case，格式：`chain-cloud-ms-{微服务名}` | `"chain-cloud-ms-base"`、`"chain-cloud-ms-transport"`、`"chain-cloud-ms-oms"` |
| `contextId` | **大驼峰**，唯一标识该 Feign bean | `"Order"`、`"TruckPlan"`、`"CostConfiguration"` |
| `path` | **加前导斜杠** | `"/order"`、`"/base/truck"`、`"/sys/driver"` |

> 注意：`name` 对应 Nacos 中注册的微服务名，`contextId` 用于避免 Spring 容器中多个 Feign Client 的 bean 名称冲突。

#### 4.2 `@ApiIgnore`
**所有** Feign 接口必须添加 `@ApiIgnore` 注解，避免 Feign 接口出现在 Swagger/Knife4j 文档中（Feign 是内部 RPC 契约，非 REST 控制器）：

```java
@FeignClient(name = "chain-cloud-ms-base", contextId = "Area", path = "/area")
@ApiIgnore
public interface AreaFeign {
    // ...
}
```

### 5. 方法命名规范

| 前缀 | 语义 | 示例 |
|------|------|------|
| `save*` | 创建实体 | `saveTruck()`、`saveDriver()`、`saveGoodsType()` |
| `findById` / `findOne*` | 单实体查询 | `findById()`、`findOneDriver()`、`fineById()` |
| `findAll*` | 批量查询 | `findAll()`、`findAll(@RequestParam("ids") List<Long> ids)` |
| `findBy*` | 条件查询 | `findByPage()`、`findByOrderId()`、`findByTruckId()` |
| `query*` | 读操作（transport 领域统一用 `query`） | `queryById()`、`queryShortestPath()`、`queryPageList()` |
| `get*` | 简单读取 | `get()`、`getByCode()`、`getById()` |
| `update*` | 更新实体 | `update()`、`updateById()`、`updateRunStatus()` |
| `delete*` / `del` | 删除实体 | `delete()`、`deleteLine()`、`del()` |
| `disable` / `enable` | 启用/禁用 | `disable(@PathVariable("id") Long id)` |
| `count*` / `count` | 统计计数 | `count(@RequestParam("memberId") Long memberId)` |
| `batch*` | 批量操作 | `batchSaveTruckDriver()`、`batchRead()` |
| `create*` / `add*` | 创建（备选） | `createLine()`、`add()` |
| `pull*` | 拉取/分配 | `pullUnassignedPlan()` |

> 注意：方法名要准确反映操作语义，read 操作用 `find`/`query`/`get` 前缀均可，但在一个领域内保持统一。

### 6. 参数注解规范

#### 6.1 `@PathVariable`
- **必须指定变量名**（显式 `name` 或简写 `value`）
- 推荐使用 `name` 属性显式声明：

```java
@GetMapping("/{id}")
OrderDTO findById(@PathVariable(name = "id") Long id);
```

#### 6.2 `@RequestParam`
- **必须指定请求参数名**
- 可选参数使用 `required = false`：

```java
@GetMapping
List<OrderCargoDTO> findAll(@RequestParam(name = "tranOrderId", required = false) Long tranOrderId,
                            @RequestParam(name = "orderId", required = false) Long orderId);
```

#### 6.3 `@RequestBody`
- POST/PUT 请求体参数
- 单个 DTO 作为参数：

```java
@PostMapping
OrderCargoDTO save(@RequestBody OrderCargoDTO dto);
```

- 批量操作可使用 `List<DTO>`：

```java
@PostMapping("batch")
void batchSave(@RequestBody List<OrderCargoDTO> dtos);
```

### 7. HTTP 方法映射规范

| HTTP 方法 | 语义 | 示例 |
|-----------|------|------|
| `@GetMapping` | 查询操作 | `findById()`、`findAll()`、`list()` |
| `@PostMapping` | 创建操作 + 分页查询 | `save()`、`findByPage()` |
| `@PutMapping` | 更新操作 | `update()`、`updateStatus()` |
| `@DeleteMapping` | 删除操作（较少使用） | `del()`、`deleteLine()` |

### 8. 返回类型规范

| 返回类型 | 用途 | 示例 |
|---------|------|------|
| `DTO` | 单实体查询 | `AreaDTO`、`OrderDTO` |
| `List<DTO>` | 批量查询 | `List<AreaDTO>`、`List<TruckDTO>` |
| `PageResponse<DTO>` | 分页查询，来自 `com.itheima.chaincloud.common.util.PageResponse` | `PageResponse<OrderDTO>` |
| `void` | 无返回值的操作（创建、更新、删除） | `void orderPickup(OrderPickupDTO)` |
| `boolean` | 成功/失败指示 | `boolean del(Long id)` |
| `Integer` | 计数 | `Integer countType(MessageQueryDTO)` |
| `Map<K, V>` | 聚合数据 | `Map<Integer, Long> count()` |
| `String` | 树形结构等特殊返回 | `String findAllTree()` |

> 注意：不使用 `ResponseEntity<>` 包装返回值。

### 9. `@SpringQueryMap` 使用规范

当 GET 请求需要传递多个查询参数时，使用 DTO 封装参数，并用 `@SpringQueryMap` 注解展开为 URL 查询参数：

```java
@GetMapping("list")
List<MessageDTO> list(@SpringQueryMap MessageQueryDTO queryDTO);
```

**使用场景**：
- 仅用于 `@GetMapping` 方法
- 查询条件字段较多（超过 3 个）时使用
- DTO 中的字段名会直接映射为 URL 查询参数名

### 10. 导入规范

```java
// 必需导入
import org.springframework.cloud.openfeign.FeignClient;
import springfox.documentation.annotations.ApiIgnore;

// Spring Web 注解（推荐显式按需导入，也可用通配符）
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
// 或使用通配符
// import org.springframework.web.bind.annotation.*;

// 特殊注解按需导入
import org.springframework.cloud.openfeign.SpringQueryMap;  // 使用@SpringQueryMap时导入

// 领域 DTO
import com.itheima.chaincloud.domain.xxx.xxx.XxxDTO;

// 通用分页
import com.itheima.chaincloud.common.util.PageResponse;
```

### 11. 完整示例

**正确示例：**

```java
package com.itheima.chaincloud.api.oms;

import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.oms.domain.OrderDTO;
import com.itheima.chaincloud.domain.oms.domain.OrderSearchDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@FeignClient(name = "chain-cloud-ms-oms", contextId = "Order", path = "/order")
@ApiIgnore
public interface OrderFeign {

    /**
     * 根据id获取订单详情
     *
     * @param id 订单Id
     * @return 订单详情
     */
    @GetMapping("/{id}")
    OrderDTO findById(@PathVariable(name = "id") Long id);

    /**
     * 获取订单分页数据
     *
     * @param orderSearchDTO 查询条件
     * @return 订单分页数据
     */
    @PostMapping("/page")
    PageResponse<OrderDTO> findByPage(@RequestBody OrderSearchDTO orderSearchDTO);
}
```

**错误示例：**

```java
// 错误1：缺少 @ApiIgnore，导致 Feign 接口出现在 Swagger 文档中
@FeignClient(name = "chain-cloud-ms-base", contextId = "Area", path = "/area")
public interface AreaFeign {  // 缺少 @ApiIgnore
}

// 错误2：contextId 使用小驼峰（应使用大驼峰）
@FeignClient(name = "chain-cloud-ms-base", contextId = "area", path = "/area")  // contextId 应为 "Area"
@ApiIgnore
public interface AreaFeign {
}

// 错误3：路径无前导斜杠（不一致，推荐统一加斜杠）
@FeignClient(name = "chain-cloud-ms-base", contextId = "Area", path = "area")  // 推荐 "/area"
@ApiIgnore
public interface AreaFeign {
}

// 错误4：@PathVariable/@RequestParam 缺少变量名
@GetMapping("/{id}")
OrderDTO findById(@PathVariable Long id);  // 缺少 name = "id"

// 错误5：GET 请求用 @RequestBody（应使用 @SpringQueryMap 或 @RequestParam）
@GetMapping("/list")
List<OrderDTO> list(@RequestBody OrderSearchDTO orderSearchDTO);  // GET不应有@RequestBody

// 错误6：Feign 接口使用 @Api 或 @ApiOperation（这些应放在 controller 层）
@Api(tags = "订单管理")
@FeignClient(name = "chain-cloud-ms-oms", contextId = "Order", path = "/order")
@ApiIgnore  // @ApiIgnore 和 @Api 同时存在会矛盾
public interface OrderFeign {
}
```

### 12. 注意事项

- **不含实现逻辑**：Feign 接口必须是纯 `interface`，不能包含任何实现逻辑。所有接口文件都不应有 `import` 实现类。
- **`@FeignClient.name`** 必须与 Nacos 中注册的微服务名完全一致。
- **`@FeignClient.contextId`** 必须在整个 Spring 容器中唯一，即使 `name` 不同也不能重复。
- **`@ApiIgnore`** 必须添加，这是强制要求（唯一例外是如果该 Feign 接口同时作为 REST 控制器暴露，但目前项目中不存在此类情况）。
- **不使用 `fallback` / `fallbackFactory`**：项目中没有使用服务降级配置。
- **不使用 `ResponseEntity<>`** 包装返回类型。
- **跨模块引用**：Feign 接口只引用 `chain-cloud-ms-domain`（DTO/枚举）和 `chain-cloud-common`（如 `PageResponse`）中的类型，不引用其他微服务的内部类型。
- **Swagger 注解**：`@ApiOperation`、`@ApiImplicitParams` 等不应出现在 Feign 接口中，这些应放在 Controller 实现层。
