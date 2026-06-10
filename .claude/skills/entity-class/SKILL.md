---
name: entity-class
description: 定义项目中 Entity 实体类编写规范，包括命名规范、注解要求、基类继承和字段映射
---

## 编写规范

### 1. 文件命名
- 文件名必须以 `Entity` 结尾（全大写）
- 示例：`AreaEntity.java`、`TruckEntity.java`、`OrderEntity.java`

### 2. 类命名
- 类名与文件名保持一致
- 使用大驼峰命名，以 `Entity` 结尾

### 3. 包结构规范
- 包名：`com.itheima.ms.<服务名>.entity[.<子域>]`
- 子域按业务分组：`base/`、`truck/`、`user/` 等
- 示例：
  - `com.itheima.ms.base.entity.base.AreaEntity`
  - `com.itheima.ms.base.entity.truck.TruckEntity`
  - `com.itheima.ms.oms.entity.OrderEntity`

### 4. 基类继承
**绝大多数 Entity 必须继承 `BaseEntity`**：

```java
public class TruckEntity extends BaseEntity {
    // 业务字段
}
```

`BaseEntity` 已提供以下字段（无需重复声明）：
- `id`（Long，`@TableId`）
- `created`（LocalDateTime，`@TableField(fill = FieldFill.INSERT)`）
- `updated`（LocalDateTime，`@TableField(fill = FieldFill.INSERT_UPDATE)`）

### 5. 必需注解

#### 5.1 类级注解
```java
@Data
@TableName("cc_xxx")
public class TruckEntity extends BaseEntity {
```

| 注解 | 必需 | 说明 |
|------|------|------|
| `@Data` | 是 | Lombok getter/setter |
| `@TableName("cc_xxx")` | 是 | MyBatis-Plus 表名映射，**表名统一 `cc_` 前缀** |
| `@Builder` | 可选 | 需要 Builder 模式时添加 |
| `@NoArgsConstructor` | 可选 | 使用 `@Builder` 时必须同时添加 |
| `@AllArgsConstructor` | 可选 | 使用 `@Builder` 时必须同时添加 |

> 当使用 `@Builder` 时，必须同时添加 `@NoArgsConstructor` 和 `@AllArgsConstructor`，三者成组出现。

#### 5.2 字段注解
- **不需要** 为继承自 `BaseEntity` 的字段（`id`、`created`、`updated`）添加注解
- 普通字段无需额外注解，MyBatis-Plus 自动驼峰转下划线映射
- 特殊映射使用 `@TableField("column_name")`
- 非数据库字段使用 `@TableField(exist = false)`

### 6. 字段文档
- 每个字段必须添加 `/** */` 格式的 Javadoc 注释
- 注释应说明字段的业务含义
- 枚举类型字段使用 `@see` 引用对应枚举类

```java
/**
 * 状态 0：禁用 1：正常
 */
private Integer status;

/**
 * 运输状态
 * @see com.itheima.chaincloud.domain.base.enums.TruckWorkStatusEnum
 */
private Integer workStatus;
```

### 7. 序列化
- 继承 `BaseEntity` 的类已实现 `Serializable`
- 可选择性声明 `serialVersionUID`（推荐添加）

```java
private static final long serialVersionUID = 1L;
```

### 8. 导入语句

```java
// 必需导入
import com.baomidou.mybatisplus.annotation.TableName;
import com.itheima.chaincloud.common.entity.BaseEntity;
import lombok.Data;

// 使用 Builder 时额外导入
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 按需导入
import java.math.BigDecimal;
import java.time.LocalDateTime;
```

### 9. 完整示例

**正确示例：**
```java
package com.itheima.ms.base.entity.truck;

import com.baomidou.mybatisplus.annotation.TableName;
import com.itheima.chaincloud.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 车辆信息表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cc_truck")
public class TruckEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 车辆类型id
     */
    private Long truckTypeId;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 车牌号码
     */
    private String licensePlate;

    /**
     * 准载重量
     */
    private BigDecimal allowableLoad;

    /**
     * 准载体积
     */
    private BigDecimal allowableVolume;

    /**
     * 状态 0：禁用 1：正常
     */
    private Integer status;

    /**
     * 运输状态
     * @see com.itheima.chaincloud.domain.base.enums.TruckWorkStatusEnum
     */
    private Integer workStatus;
}
```

**简单示例（无 Builder）：**
```java
package com.itheima.ms.base.entity.base;

import com.baomidou.mybatisplus.annotation.TableName;
import com.itheima.chaincloud.common.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 行政区域
 */
@Data
@TableName("cc_area")
public class AreaEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 父级行政区域id
     */
    private Long parentId;

    /**
     * 行政区域名称
     */
    private String name;

    /**
     * 行政区域编码
     */
    private String areaCode;
}
```

**错误示例：**
```java
// 错误1：缺少 @TableName
@Data
public class TruckEntity extends BaseEntity {  // 缺少 @TableName("cc_truck")
}

// 错误2：表名缺少 cc_ 前缀
@TableName("truck")  // 应为 "cc_truck"
public class TruckEntity extends BaseEntity {
}

// 错误3：重复声明 BaseEntity 已有的字段
public class TruckEntity extends BaseEntity {
    private Long id;  // 错误：BaseEntity 已提供 id
    private LocalDateTime created;  // 错误：BaseEntity 已提供 created
}

// 错误4：@Builder 缺少配套注解
@Data
@Builder
@TableName("cc_truck")
public class TruckEntity extends BaseEntity {  // 缺少 @NoArgsConstructor 和 @AllArgsConstructor
}

// 错误5：字段缺少注释
public class TruckEntity extends BaseEntity {
    private String brand;  // 缺少 Javadoc 注释
}
```

### 10. 注意事项

- **表名统一 `cc_` 前缀**：所有表名以 `cc_` 开头，如 `cc_truck`、`cc_order`、`cc_area`
- **不重复声明基类字段**：`id`、`created`、`updated` 由 `BaseEntity` 提供
- **字段自动填充**：`created` 和 `updated` 由 `MyMetaObjectHandler` 自动填充，无需手动赋值
- **枚举字段存储为 Integer**：枚举值通过 `@EnumValue` 标注的 `code` 字段存储到数据库
- **金额用 BigDecimal**：涉及金额的字段使用 `BigDecimal`，不要用 `Double`
- **时间用 LocalDateTime**：时间字段统一使用 `LocalDateTime`，不要用 `Date`
