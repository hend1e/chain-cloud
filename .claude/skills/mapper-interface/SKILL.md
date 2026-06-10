---
name: mapper-interface
description: 定义项目中 MyBatis-Plus Mapper 接口编写规范，包括命名规范、注解要求和基类继承
---

## 编写规范

### 1. 文件命名
- 文件名必须以 `Mapper` 结尾（全大写）
- 示例：`AreaMapper.java`、`TruckMapper.java`、`OrderMapper.java`

### 2. 类命名
- 类名与文件名保持一致
- 使用大驼峰命名，以 `Mapper` 结尾
- 名称与对应的 Entity 类名一致（去掉 `Entity` 后缀，加上 `Mapper`）

| Entity | Mapper |
|--------|--------|
| `AreaEntity` | `AreaMapper` |
| `TruckEntity` | `TruckMapper` |
| `OrderEntity` | `OrderMapper` |

### 3. 包结构规范
- 包名：`com.itheima.ms.<服务名>.mapper[.<子域>]`
- 子域与 Entity 包结构对齐：`base/`、`truck/`、`user/` 等
- 示例：
  - `com.itheima.ms.base.mapper.base.AreaMapper`
  - `com.itheima.ms.base.mapper.truck.TruckMapper`
  - `com.itheima.ms.oms.mapper.OrderMapper`

### 4. 必需继承

**必须继承 `BaseMapper<Entity>`**：

```java
public interface TruckMapper extends BaseMapper<TruckEntity> {
}
```

`BaseMapper<T>` 提供了以下内置方法（无需手动编写）：
- `insert(entity)` — 插入
- `deleteById(id)` — 根据 ID 删除
- `updateById(entity)` — 根据 ID 更新
- `selectById(id)` — 根据 ID 查询
- `selectList(wrapper)` — 条件查询列表
- `selectPage(page, wrapper)` — 分页查询
- `selectCount(wrapper)` — 统计数量

### 5. 必需注解

**必须添加 `@Mapper` 注解**：

```java
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TruckMapper extends BaseMapper<TruckEntity> {
}
```

> `@Mapper` 让 MyBatis 自动识别并创建该接口的代理实现。

### 6. 导入语句

```java
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.ms.xxx.entity.XxxEntity;
import org.apache.ibatis.annotations.Mapper;
```

### 7. 完整示例

**标准示例（最简形式）：**
```java
package com.itheima.ms.base.mapper.truck;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.ms.base.entity.truck.TruckEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TruckMapper extends BaseMapper<TruckEntity> {
}
```

**带注释的示例：**
```java
package com.itheima.ms.base.mapper.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.ms.base.entity.base.AreaEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper 接口
 * 行政区域
 */
@Mapper
public interface AreaMapper extends BaseMapper<AreaEntity> {

}
```

**错误示例：**
```java
// 错误1：缺少 @Mapper 注解
public interface TruckMapper extends BaseMapper<TruckEntity> {  // 缺少 @Mapper
}

// 错误2：未继承 BaseMapper
@Mapper
public interface TruckMapper {  // 缺少 extends BaseMapper<TruckEntity>
}

// 错误3：泛型参数错误
@Mapper
public interface TruckMapper extends BaseMapper<TruckMapper> {  // 应为 TruckEntity
}

// 错误4：命名不一致
@Mapper
public interface TruckDao extends BaseMapper<TruckEntity> {  // 应为 TruckMapper
}
```

### 8. 自定义方法（极少使用）

项目中绝大多数 Mapper 接口都是空的（仅继承 `BaseMapper`），所有 CRUD 操作通过 `ServiceImpl` 中的 `LambdaQueryWrapper` 完成。

**仅在以下情况添加自定义方法：**
- 复杂的多表关联查询（MyBatis-Plus Wrapper 无法满足）
- 需要自定义 SQL 的特殊场景

自定义方法需配合 XML 映射文件：

```java
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {

    /**
     * 根据用户ID统计订单数量
     *
     * @param memberId 用户ID
     * @return 订单数量
     */
    Integer countByMemberId(@Param("memberId") Long memberId);
}
```

对应 XML 文件放在 `resources/mapper/` 目录下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.itheima.ms.oms.mapper.OrderMapper">
    <select id="countByMemberId" resultType="java.lang.Integer">
        SELECT COUNT(*) FROM cc_order WHERE member_id = #{memberId}
    </select>
</mapper>
```

### 9. 注意事项

- **接口必须是纯 `interface`**：不能是 `class` 或 `abstract class`
- **不需要实现类**：MyBatis 会自动创建代理实现
- **不需要 `@Repository`**：`@Mapper` 已足够
- **泛型参数是 Entity 类**：`BaseMapper<XxxEntity>`，不是 DTO
- **通常不需要自定义方法**：优先使用 `LambdaQueryWrapper` 在 Service 层构建查询
- **与 ServiceImpl 对应**：每个 Mapper 对应一个 `ServiceImpl<ThisMapper, ThisEntity>`
