---
name: vo-class
description: 定义项目中 VO（View Object）类编写规范，用于 Web 模块的前端数据交互
---

## 编写规范

### 1. 文件命名

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| 通用 VO | `{业务名}VO.java` | `TruckVO.java`、`OrderVO.java` |
| 查询 VO | `{业务名}QueryVO.java` | `OrderQueryVO.java`、`TransportOrderQueryVO.java` |
| 新增 VO | `{业务名}AddVO.java` | `WorkbenchAddVO.java` |
| 更新 VO | `{业务名}UpdateVO.java` | `TransportTripsUpdateVO.java` |
| 列表 VO | `{业务名}ListVO.java` | `TruckReturnRegisterListVO.java` |

### 2. 类命名
- 类名与文件名保持一致
- 使用大驼峰命名，以 `VO` 结尾

### 3. 包结构规范
- 包名：`com.itheima.ms.web.<模块名>.vo[.<子域>]`
- 子域按业务分组：`baseTruck/`、`oms/`、`work/`、`agency/` 等
- 示例：
  - `com.itheima.ms.web.manager.vo.baseTruck.TruckVO`
  - `com.itheima.ms.web.manager.vo.oms.OrderQueryVO`
  - `com.itheima.ms.web.customer.vo.oms.MailingSaveVO`

### 4. 必需注解

#### 4.1 类级注解
```java
@Data
@ApiModel(value = "车辆信息")
public class TruckVO {
```

| 注解 | 必需 | 说明 |
|------|------|------|
| `@Data` | 是 | Lombok getter/setter |
| `@ApiModel` | 推荐 | Swagger 模型描述 |

#### 4.2 字段注解
**每个字段必须添加 `@ApiModelProperty`**：
```java
@ApiModelProperty(value = "车牌号码")
private String licensePlate;
```

### 5. 参数校验注解

使用 `javax.validation.constraints` 进行参数校验：

```java
@ApiModelProperty(value = "长，单位cm")
@Min(value = 1, message = "长度最小为1")
@Max(value = 999, message = "长度最大为999")
private Integer measureLong;
```

| 注解 | 用途 | 示例 |
|------|------|------|
| `@NotNull` | 非空校验 | `@NotNull(message = "id不能为空")` |
| `@NotBlank` | 字符串非空 | `@NotBlank(message = "名称不能为空")` |
| `@Min` | 最小值 | `@Min(value = 1, message = "最小为1")` |
| `@Max` | 最大值 | `@Max(value = 999, message = "最大为999")` |
| `@Size` | 长度范围 | `@Size(min = 1, max = 50, message = "长度1-50")` |
| `@Email` | 邮箱格式 | `@Email(message = "邮箱格式不正确")` |
| `@Pattern` | 正则匹配 | `@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")` |

### 6. 日期格式化

`LocalDateTime` 字段添加 `@JsonFormat`：
```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime createTime;
```

### 7. 字段文档

- 优先使用 `@ApiModelProperty(value = "xxx")` 作为字段说明
- 可选添加 Javadoc 注释补充详细信息

```java
/**
 * 运输状态 0：已到达 1：运输中
 */
@ApiModelProperty(value = "运输状态 0：已到达 1：运输中")
private Integer runStatus;
```

### 8. 默认值

部分字段可设置默认值：
```java
@ApiModelProperty(value = "准载重量")
private BigDecimal allowableLoad = new BigDecimal("0.0");
```

### 9. 导入语句

```java
// 必需导入
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

// 参数校验（按需）
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

// 日期格式化（按需）
import com.fasterxml.jackson.annotation.JsonFormat;

// 数据类型
import java.math.BigDecimal;
import java.time.LocalDateTime;
```

### 10. 完整示例

**通用 VO 示例：**
```java
package com.itheima.ms.web.manager.vo.baseTruck;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(value = "车辆信息")
public class TruckVO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "车辆类型Id")
    private Long truckTypeId;

    @ApiModelProperty(value = "车辆类型名称")
    private String truckTypeName;

    @ApiModelProperty(value = "品牌")
    private String brand;

    @ApiModelProperty(value = "车牌号码")
    private String licensePlate;

    @ApiModelProperty(value = "准载重量")
    private BigDecimal allowableLoad = new BigDecimal("0.0");

    @ApiModelProperty(value = "状态 0：禁用 1：正常")
    private Integer status;
}
```

**查询 VO 示例（带分页参数）：**
```java
package com.itheima.ms.web.manager.vo.oms;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderQueryVO {

    @ApiModelProperty(value = "页码")
    private Integer page;

    @ApiModelProperty(value = "页尺寸")
    private Integer pageSize;

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "订单类型，1为同城订单，2为城际订单")
    private Integer orderType;

    @ApiModelProperty(value = "下单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "订单状态")
    private Integer status;
}
```

**带校验的 VO 示例：**
```java
package com.itheima.ms.web.customer.vo.oms;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
public class MailingSaveVO {

    @ApiModelProperty("发件方地址簿id")
    private Long sendAddress;

    @ApiModelProperty("收件方地址簿id")
    private Long receiptAddress;

    @ApiModelProperty("物品名称")
    private String goodsName;

    @ApiModelProperty(value = "长，单位cm")
    @Min(value = 1, message = "长度最小为1")
    @Max(value = 999, message = "长度最大为999")
    private Integer measureLong;

    @ApiModelProperty(value = "宽，单位cm")
    @Min(value = 1, message = "宽度最小为1")
    @Max(value = 999, message = "宽度最大为999")
    private Integer measureWidth;

    @ApiModelProperty(value = "高，单位cm")
    @Min(value = 1, message = "高度最小为1")
    @Max(value = 999, message = "高度最大为999")
    private Integer measureHigh;
}
```

**错误示例：**
```java
// 错误1：缺少 @ApiModelProperty
@Data
public class TruckVO {
    private String licensePlate;  // 缺少 @ApiModelProperty
}

// 错误2：命名不规范
@Data
public class TruckInfo {  // 应以 VO 结尾：TruckVO
}

// 错误3：LocalDateTime 缺少 @JsonFormat
@Data
public class OrderVO {
    private LocalDateTime createTime;  // 缺少 @JsonFormat
}

// 错误4：VO 继承 Entity（不应继承）
@Data
public class TruckVO extends TruckEntity {  // 错误：VO 不应继承 Entity
}
```

### 11. VO 与 DTO 的区别

| 特性 | VO | DTO |
|------|-----|-----|
| 使用位置 | Web 模块（面向前端） | 服务间通信（Feign） |
| 包路径 | `com.itheima.ms.web.*.vo` | `com.itheima.chaincloud.domain.*` |
| Swagger 注解 | `@ApiModelProperty` 必需 | 不需要 |
| 校验注解 | `@NotNull`、`@Min` 等 | 一般不加 |
| 基类 | 无 | 无 |
| 命名后缀 | `VO`、`QueryVO`、`AddVO` | `DTO`、`QueryDTO` |

### 12. 注意事项

- **VO 不继承任何基类**：与 Entity 不同，VO 是独立的扁平对象
- **每个字段必须有 `@ApiModelProperty`**：前端依赖此注解生成文档
- **分页参数放在 QueryVO 中**：`page`、`pageSize` 作为字段
- **日期字段必须加 `@JsonFormat`**：确保前后端日期格式一致
- **Controller 中配合 `@Validated` 使用**：启用参数校验
- **VO 与 DTO 转换**：使用 `BeanUtil.toBean(dto, XxxVO.class)` 进行转换
