---
name: exception-design
description: 定义项目中异常处理规范，包括 CCException/CCWebException 使用、异常枚举定义和全局异常处理
---

## 编写规范

### 1. 异常体系概述

项目有两种自定义异常，用于不同场景：

| 异常类 | 使用场景 | HTTP 状态码 | 响应格式 |
|--------|---------|------------|---------|
| `CCException` | 业务微服务（Service Controller） | 自定义（默认 500） | `{"code": xxx, "msg": "xxx"}` |
| `CCWebException` | Web 模块（Web Controller） | 始终 200 | `R.error(data, msg)` |

### 2. CCException 规范

#### 2.1 适用场景
- 业务微服务模块（`chain-cloud-ms-*-service`）
- Feign 调用链路中的异常传播
- 需要返回非 200 HTTP 状态码的场景

#### 2.2 字段说明
```java
public class CCException extends RuntimeException {
    private String msg;     // 异常消息
    private int code = 1001; // 业务状态码（4位数，从1001开始递增）
    private int status = 500; // HTTP 状态码
}
```

#### 2.3 构造方式
```java
// 方式1：简单消息（code=1001, status=500）
throw new CCException("车辆不存在");

// 方式2：消息 + 自定义 code
throw new CCException("车辆已被禁用", 1002);

// 方式3：消息 + code + status
throw new CCException("参数错误", 1003, 400);

// 方式4：使用枚举
throw new CCException(TradingEnum.NOT_FOUND);

// 方式5：带原始异常
throw new CCException("支付失败", e);
```

#### 2.4 使用场景示例
```java
// Service 层
@Override
public TruckEntity getById(Long id) {
    TruckEntity entity = super.getById(id);
    if (ObjectUtil.isEmpty(entity)) {
        throw new CCException("车辆不存在");
    }
    return entity;
}

// 使用枚举
@Override
public void createTrading(TradingEntity trading) {
    if (ObjectUtil.isEmpty(config)) {
        throw new CCException(TradingEnum.CONFIG_EMPTY);
    }
}
```

### 3. CCWebException 规范

#### 3.1 适用场景
- Web 模块（`chain-cloud-ms-web-*`）
- 面向前端的接口
- HTTP 状态码始终返回 200，错误信息在响应体中

#### 3.2 字段说明
```java
public class CCWebException extends RuntimeException {
    private String msg;    // 异常消息
    private int code = 1;  // 业务状态码（异常弹窗状态码）
    private int status = 200; // HTTP 状态码（始终 200）
}
```

#### 3.3 构造方式
```java
// 方式1：简单消息
throw new CCWebException("操作失败");

// 方式2：消息 + 自定义 code
throw new CCWebException("数据不存在", 1001);

// 方式3：使用枚举
throw new CCWebException(WebExceptionEnum.DATA_NOT_FOUND);
```

### 4. 异常枚举规范

#### 4.1 实现 `BaseExceptionEnum`
```java
package com.itheima.ms.trade.enums;

import com.itheima.chaincloud.common.enums.BaseExceptionEnum;

public enum TradingEnum implements BaseExceptionEnum {

    SUCCEED(1001, 200, "操作成功"),
    ERROR(1002, "操作失败"),
    CONFIG_EMPTY(1008, "支付配置为空"),
    NATIVE_PAY_FAIL(1010, "统一下单交易失败"),
    NOT_FOUND(1022, "交易单不存在");

    private Integer code;
    private Integer status;
    private String value;

    // 默认 status=500
    TradingEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
        this.status = 500;
    }

    // 自定义 status
    TradingEnum(Integer code, Integer status, String value) {
        this.code = code;
        this.value = value;
        this.status = status;
    }

    @Override
    public Integer getCode() { return this.code; }

    @Override
    public String getValue() { return this.value; }

    @Override
    public Integer getStatus() { return this.status; }
}
```

| 要素 | 规范 |
|------|------|
| 实现接口 | `BaseExceptionEnum`（继承 `BaseEnum`） |
| `code` | 业务状态码，4位数，从1001开始递增 |
| `status` | HTTP 状态码，默认 500 |
| `value` | 错误消息描述 |
| 命名 | `{模块名}Enum`，如 `TradingEnum`、`ExceptionEnum` |

#### 4.2 命名规范
- 枚举常量：大写下划线，如 `NOT_FOUND`、`CONFIG_EMPTY`
- 枚举类名：`{模块}Enum` 或 `ExceptionEnum`

#### 4.3 包位置
- 各模块独立定义：`com.itheima.ms.{模块}.enums.{模块}Enum`
- 示例：
  - `com.itheima.ms.trade.enums.TradingEnum`
  - `com.itheima.ms.transport.enums.ExceptionEnum`

### 5. 全局异常处理器

`GlobalExceptionHandler` 统一处理所有异常：

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 参数校验异常 → HTTP 400
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handle(ValidationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("code", 400, "msg", errors));
    }

    // CCException → 自定义 HTTP 状态码
    @ExceptionHandler(CCException.class)
    public ResponseEntity<Object> handle(CCException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(Map.of("code", exception.getCode(), "msg", exception.getMsg()));
    }

    // CCWebException → HTTP 200 + R.error()
    @ExceptionHandler(CCWebException.class)
    public R<Map<String, Object>> handle(CCWebException exception) {
        return R.error(Map.of("code", exception.getCode(), "msg", exception.getMsg()), 
                       exception.getMsg());
    }

    // 其他异常 → HTTP 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handle(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", 500, "msg", exception.getMessage()));
    }
}
```

### 6. 异常选择决策流程

```
发生业务异常
    │
    ├─ 在业务微服务（chain-cloud-ms-*-service）？
    │   └─ 使用 CCException
    │       ├─ throw new CCException("消息")
    │       └─ throw new CCException(XxxEnum.VALUE)
    │
    └─ 在 Web 模块（chain-cloud-ms-web-*）？
        └─ 使用 CCWebException
            ├─ throw new CCWebException("消息")
            └─ throw new CCWebException(XxxEnum.VALUE)
```

### 7. 导入语句

```java
// 异常类
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.exception.CCWebException;

// 异常枚举接口
import com.itheima.chaincloud.common.enums.BaseExceptionEnum;
import com.itheima.chaincloud.common.enums.BaseEnum;
```

### 8. 完整示例

**异常枚举：**
```java
package com.itheima.ms.transport.enums;

import com.itheima.chaincloud.common.enums.BaseExceptionEnum;

public enum ExceptionEnum implements BaseExceptionEnum {

    ORGAN_NOT_FOUND(1001, "机构不存在"),
    START_END_ORGAN_NOT_FOUND(1002, "起点、终点机构不能为空"),
    TRANSPORT_LINE_NOT_FOUND(1004, "路线不存在"),
    TRANSPORT_LINE_ORGAN_CANNOT_SAME(1007, "起点、终点机构不能相同");

    private Integer code;
    private Integer status;
    private String value;

    ExceptionEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
        this.status = 500;
    }

    @Override
    public Integer getCode() { return this.code; }

    @Override
    public String getValue() { return this.value; }

    @Override
    public Integer getStatus() { return this.status; }
}
```

**Service 中使用：**
```java
@Service
public class TransportLineServiceImpl {

    public TransportLine queryById(Long id) {
        TransportLine line = baseMapper.selectById(id);
        if (ObjectUtil.isEmpty(line)) {
            throw new CCException(ExceptionEnum.TRANSPORT_LINE_NOT_FOUND);
        }
        return line;
    }

    public void createLine(Long startOrganId, Long endOrganId) {
        if (ObjectUtil.equal(startOrganId, endOrganId)) {
            throw new CCException(ExceptionEnum.TRANSPORT_LINE_ORGAN_CANNOT_SAME);
        }
        // 创建逻辑
    }
}
```

**错误示例：**
```java
// 错误1：使用 RuntimeException
throw new RuntimeException("操作失败");  // 错误：应使用 CCException

// 错误2：在 Web 模块使用 CCException
// Web Controller 中
throw new CCException("操作失败");  // 错误：应使用 CCWebException

// 错误3：异常枚举未实现 BaseExceptionEnum
public enum MyEnum {  // 错误：缺少 implements BaseExceptionEnum
    ERROR(1001, "错误");
}

// 错误4：code 不是 4 位数
public enum MyEnum implements BaseExceptionEnum {
    ERROR(1, "错误");  // 不推荐：code 应从 1001 开始
}
```

### 9. 注意事项

- **不要使用 `RuntimeException`**：统一使用 `CCException` 或 `CCWebException`
- **异常枚举 `code` 从 1001 开始**：每个模块独立编号
- **`CCException` 的 `status` 默认 500**：可按需设置为 400、404 等
- **`CCWebException` 的 `status` 始终 200**：前端通过 `R.code` 判断成功/失败
- **异常消息用中文**：面向用户/开发者
- **不要捕获 `CCException`**：让它传播到 `GlobalExceptionHandler`
- **Feign 调用异常传播**：`FeignErrorDecoder` 会将下游的 `CCException` 重新抛出
