---
name: domain-dto
description: 定义 项目中 DTO 类编写规范，包括命名规范和注解要求
---

## 编写规范

### 1. 文件命名
- 文件名必须以 `DTO` 结尾（全大写）
- 示例：`AreaDTO.java`、`GoodsTypeDTO.java`、`MessageDTO.java`

### 2. 类命名
- 类名与文件名保持一致
- 使用驼峰命名，以 `DTO` 结尾

### 3. 必需注解
每个 DTO 类必须添加以下 Lombok 注解：

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
```

### 4. 导入语句
需要导入以下包：
```java
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
```

### 5. 示例

**正确示例：**
```java
package com.itheima.chaincloud.domain.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaDTO {
    private Long id;
    private String name;
    private String areaCode;
}
```

**错误示例：**
```java
// 文件名错误：AreaDto.java
// 类名错误：class AreaDto
// 缺少注解：@Builder, @NoArgsConstructor, @AllArgsConstructor
```

### 6. 注释规范
- 每个字段必须添加注释，说明字段含义
- 使用 `/** */` 格式的 Javadoc 注释
- 注释内容应简洁明了，说明字段的业务含义
- 示例：
  ```java
  /**
   * 主键
   */
  private Long id;
  /**
   * 行政区域名称
   */
  private String name;
  ```

### 7. 注意事项
- 文件名后缀为 `.java`
- 包名：`com.itheima.chaincloud.domain.<模块名>`
- 模块名包括：base、user、truck 等业务模块
- DTO 类放在 `src/main/java/com/itheima/chaincloud/domain/<模块名>/` 目录下
- 例如：base 模块下为 `com.itheima.chaincloud.domain.base`，user 模块下为 `com.itheima.chaincloud.domain.user`