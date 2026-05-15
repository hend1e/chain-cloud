---
name: domain-enum
description: 定义 项目中 的枚举类编写规范，包括命名规范、注解要求和实现模式
---

## 编写规范

### 1. 文件命名
- 文件名必须以 `Enum` 结尾（全大写）
- 示例：`StatusEnum.java`、`MessageBussinessTypeEnum.java`、`WorkStatusEnum.java`

### 2. 类命名
- 类名与文件名保持一致
- 使用驼峰命名，以 `Enum` 结尾
- 枚举名称应准确描述业务含义

### 3. 必需实现
每个枚举类必须实现 `BaseEnum` 接口：

```java
public enum StatusEnum implements BaseEnum {
    // 枚举值定义
}
```

### 4. 必需注解
- `@EnumValue`：MyBatis-Plus 注解，用于数据库映射
- `@JsonValue`：Jackson 注解，用于 JSON 序列化
- 这两个注解必须加在 `code` 字段上

### 5. 字段定义
每个枚举类必须包含以下字段：

```java
/**
 * 类型编码
 */
@EnumValue
@JsonValue
private final Integer code;

/**
 * 类型值
 */
private final String value;
```

### 6. 构造方法
必须提供带参数的构造方法：

```java
EnumName(Integer code, String value) {
    this.code = code;
    this.value = value;
}
```

### 7. 必需方法
必须实现 `BaseEnum` 接口的方法：

```java
@Override
public Integer getCode() {
    return code;
}

@Override
public String getValue() {
    return value;
}
```

### 8. 工具方法
建议添加静态工具方法用于根据编码获取枚举：

```java
public static EnumName codeOf(Integer code) {
    return EnumUtil.getBy(EnumName::getCode, code);
}
```

### 9. 枚举值定义规范
- 每个枚举值必须有详细的注释说明
- 注释使用 `/** */` 格式的 Javadoc
- 注释内容说明该枚举值的业务含义
- 示例：

```java
/**
 * 1：用户端
 */
USER(1, "用户端"),

/**
 * 2：司机端
 */
DRIVER(2, "司机端")
```

### 10. 示例

**正确示例：**
```java
package com.itheima.chaincloud.domain.base.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;

/**
 * 消息业务类型枚举
 * 1：用户端，2：司机端，3：快递员端，4：后台管理系统
 */
public enum MessageBussinessTypeEnum implements BaseEnum {
    
    /**
     * 1：用户端
     */
    USER(1, "用户端"),
    
    /**
     * 2：司机端
     */
    DRIVER(2, "司机端"),
    
    /**
     * 3：快递员端
     */
    COURIER(3, "快递员端"),
    
    /**
     * 4：后台管理系统
     */
    MANAGER(4, "后台管理系统");

    MessageBussinessTypeEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    /**
     * 类型编码
     */
    @EnumValue
    @JsonValue
    private final Integer code;

    /**
     * 类型值
     */
    private final String value;

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getValue() {
        return value;
    }

    public static MessageBussinessTypeEnum codeOf(Integer code) {
        return EnumUtil.getBy(MessageBussinessTypeEnum::getCode, code);
    }
}
```

**错误示例：**
```java
// 缺少 implements BaseEnum
public enum Status {
    // 缺少@EnumValue和@JsonValue注解
    private Integer code;
    private String value;
    
    // 缺少必需的方法实现
}
```

### 11. 特殊枚举类型

#### 11.1 复杂枚举（如IdEnum）
对于包含多个属性的枚举，可以扩展字段，但仍需实现BaseEnum接口。推荐使用Lombok的@Getter注解简化getter方法：

```java
public enum IdEnum implements BaseEnum {
    TRANSPORT_ORDER(1, "运单号", "transport_order", "segment", "SL");

    private final Integer code;
    private final String value;
    @Getter
    private final String biz;     // 业务名称
    @Getter
    private final String type;    // 类型：自增长（segment），雪花id（snowflake）
    @Getter
    private final String prefix;  // id前缀

    IdEnum(Integer code, String value, String biz, String type, String prefix) {
        this.code = code;
        this.value = value;
        this.biz = biz;
        this.type = type;
        this.prefix = prefix;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "IdEnum{" + "code=" + code +
                ", value='" + value + '\'' +
                ", biz='" + biz + '\'' +
                ", type='" + type + '\'' +
                ", prefix='" + prefix + '\'' +
                '}';
    }
}
```

#### 11.2 简单枚举（如StatusEnum）
对于简单的状态枚举，可以不实现BaseEnum接口，但建议统一实现：

```java
public enum StatusEnum {
    DISABLED(0, "DISABLED"),
    NORMAL(1, "NORMAL");

    // 字段、构造方法和getter方法...
}
```

### 12. 包结构规范
- 包名：`com.itheima.chaincloud.domain.<模块名>.enums`
- 模块名包括：base、user、truck 等业务模块
- 枚举类放在 `src/main/java/com/itheima/chaincloud/domain/<模块名>/enums/` 目录下

### 13. 导入依赖

#### 基础枚举必需导入：
```java
import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itheima.chaincloud.common.enums.BaseEnum;
```

#### 复杂枚举额外导入（如使用Lombok）：
```java
import lombok.Getter;
```