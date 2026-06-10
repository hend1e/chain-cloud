---
name: strategy-handler
description: 定义项目中策略模式编写规范，包括注解路由、Handler接口、工厂类和具体实现
---

## 编写规范

### 1. 模式概述

策略模式用于根据运行时条件（如支付渠道、短信通道）选择不同的实现。项目使用**自定义注解 + 工厂类**实现策略路由。

**涉及文件：**
- 自定义注解（如 `@PayChannel`、`@SendChannel`）
- Handler 接口（如 `BasicPayHandler`、`SmsSendHandler`）
- 工厂类 `HandlerFactory`
- 具体实现类（如 `WechatNativePayHandler`、`AliYunSmsSendHandler`）

### 2. 自定义注解规范

#### 2.1 注解定义
```java
package com.itheima.ms.trade.annotation;

import com.itheima.chaincloud.domain.trade.enums.PayChannelEnum;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PayChannel {
    PayChannelEnum type();
}
```

| 要素 | 规范 |
|------|------|
| `@Target` | 必须是 `ElementType.TYPE`（类级注解） |
| `@Retention` | 必须是 `RetentionPolicy.RUNTIME`（运行时可获取） |
| `@Documented` | 推荐添加 |
| `type()` | 返回枚举类型，用于标识策略类型 |

#### 2.2 命名规范
- 注解名描述策略维度：`@PayChannel`（支付渠道）、`@SendChannel`（发送渠道）
- 放在 `annotation/` 包下

### 3. Handler 接口规范

#### 3.1 接口定义
```java
package com.itheima.ms.trade.handler;

public interface BasicPayHandler {
    Boolean queryTrading(TradingEntity trading) throws CCException;
    Boolean closeTrading(TradingEntity trading) throws CCException;
    Boolean refundTrading(RefundRecordEntity refundRecord) throws CCException;
}
```

| 要素 | 规范 |
|------|------|
| 命名 | `{功能}Handler`，如 `BasicPayHandler`、`SmsSendHandler` |
| 位置 | `handler/` 包下 |
| 方法 | 定义策略的通用操作 |
| 异常 | 可抛出 `CCException` |

#### 3.2 继承接口（可选）
对于有多种操作类型的场景，可定义子接口：
```java
// 基础接口
public interface BasicPayHandler { ... }

// 子接口：扫码支付
public interface NativePayHandler extends BasicPayHandler {
    void createDownLineTrading(TradingEntity trading) throws CCException;
}

// 子接口：JSAPI支付
public interface JsapiPayHandler extends BasicPayHandler {
    void createJsapiTrading(TradingEntity trading) throws CCException;
}
```

### 4. 具体实现类规范

#### 4.1 类声明
```java
@Component("wechatNativePayHandler")
@PayChannel(type = PayChannelEnum.WECHAT_PAY)
public class WechatNativePayHandler implements NativePayHandler {
    // 实现方法
}
```

| 注解 | 必需 | 说明 |
|------|------|------|
| `@Component` | 是 | Spring Bean，**必须指定唯一名称** |
| `@PayChannel` | 是 | 自定义注解，标识策略类型 |

#### 4.2 命名规范
- 类名：`{渠道名}{功能}Handler`，如 `WechatNativePayHandler`、`AliYunSmsSendHandler`
- Bean 名称：小驼峰，如 `wechatNativePayHandler`、`aliYunSmsSendHandler`
- 位置：`handler/{渠道}/` 包下，如 `handler/wechat/`、`handler/alipay/`

#### 4.3 完整示例
```java
package com.itheima.ms.trade.handler.wechat;

import com.itheima.ms.trade.annotation.PayChannel;
import com.itheima.ms.trade.entity.TradingEntity;
import com.itheima.chaincloud.domain.trade.enums.PayChannelEnum;
import com.itheima.ms.trade.handler.NativePayHandler;
import com.itheima.chaincloud.common.exception.CCException;
import org.springframework.stereotype.Component;

@Component("wechatNativePayHandler")
@PayChannel(type = PayChannelEnum.WECHAT_PAY)
public class WechatNativePayHandler implements NativePayHandler {

    @Override
    public void createDownLineTrading(TradingEntity tradingEntity) throws CCException {
        // 微信 Native 支付实现逻辑
    }

    @Override
    public Boolean queryTrading(TradingEntity trading) throws CCException {
        // 查询交易实现
        return true;
    }
}
```

### 5. 工厂类规范

#### 5.1 工厂类定义
```java
package com.itheima.ms.trade.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.itheima.ms.trade.annotation.PayChannel;
import com.itheima.chaincloud.domain.trade.enums.PayChannelEnum;

import java.util.Map;

public class HandlerFactory {

    private HandlerFactory() {
        // 私有构造，防止实例化
    }

    public static <T> T get(PayChannelEnum payChannel, Class<T> handler) {
        Map<String, T> beans = SpringUtil.getBeansOfType(handler);
        for (Map.Entry<String, T> entry : beans.entrySet()) {
            PayChannel annotation = entry.getValue().getClass().getAnnotation(PayChannel.class);
            if (ObjectUtil.isNotEmpty(annotation) && ObjectUtil.equal(payChannel, annotation.type())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static <T> T get(String payChannel, Class<T> handler) {
        return get(PayChannelEnum.valueOf(payChannel), handler);
    }
}
```

| 要素 | 规范 |
|------|------|
| 类名 | `HandlerFactory` |
| 构造 | 私有，防止实例化 |
| `get()` 方法 | 静态方法，根据枚举获取实现 |
| 原理 | 遍历所有 Bean，匹配注解中的 `type()` |

#### 5.2 使用方式
```java
// 获取指定渠道的 Handler
NativePayHandler handler = HandlerFactory.get(PayChannelEnum.WECHAT_PAY, NativePayHandler.class);
handler.createDownLineTrading(tradingEntity);
```

### 6. 配套枚举规范

策略类型通常由枚举定义：
```java
package com.itheima.chaincloud.domain.trade.enums;

public enum PayChannelEnum {
    WECHAT_PAY,
    ALIPAY;
}
```

### 7. 包结构规范

```
com.itheima.ms.trade/
├── annotation/
│   └── PayChannel.java              -- 自定义注解
├── handler/
│   ├── BasicPayHandler.java         -- 基础接口
│   ├── NativePayHandler.java        -- 子接口（可选）
│   ├── HandlerFactory.java          -- 工厂类
│   ├── wechat/
│   │   ├── WechatNativePayHandler.java
│   │   └── WechatJsapiPayHandler.java
│   └── alipay/
│       ├── AliNativePayHandler.java
│       └── AliJsapiPayHandler.java
```

### 8. 完整示例：短信发送策略

**注解：**
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SendChannel {
    SendChannelEnum type();
}
```

**接口：**
```java
public interface SmsSendHandler {
    void send(SmsThirdChannelEntity channel, List<SmsRecordEntity> records);
}
```

**实现：**
```java
@Slf4j
@Component
@SendChannel(type = SendChannelEnum.ALI_YUN)
public class AliYunSmsSendHandler implements SmsSendHandler {

    @Override
    public void send(SmsThirdChannelEntity channel, List<SmsRecordEntity> records) {
        // 阿里云短信发送实现
    }
}
```

**工厂：**
```java
public class HandlerFactory {

    private HandlerFactory() {}

    public static <T> T get(SendChannelEnum channel, Class<T> handler) {
        Map<String, T> beans = SpringUtil.getBeansOfType(handler);
        for (Map.Entry<String, T> entry : beans.entrySet()) {
            SendChannel annotation = entry.getValue().getClass().getAnnotation(SendChannel.class);
            if (ObjectUtil.isNotEmpty(annotation) && ObjectUtil.equal(channel, annotation.type())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
```

### 9. 导入语句

**注解：**
```java
import java.lang.annotation.*;
```

**工厂类：**
```java
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import java.util.Map;
```

**实现类：**
```java
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
```

### 10. 注意事项

- **Bean 名称必须唯一**：`@Component("wechatNativePayHandler")` 避免冲突
- **注解 `type()` 返回枚举**：不使用字符串，保证类型安全
- **工厂类私有构造**：纯工具类，防止实例化
- **工厂方法泛型**：`<T> T get(Enum, Class<T>)` 支持不同接口类型
- **新增渠道只需添加实现**：无需修改工厂类，符合开闭原则
- **找不到实现返回 `null`**：调用方需判空或抛异常
