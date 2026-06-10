---
name: mq-listener
description: 定义项目中 RabbitMQ 消息监听器编写规范，包括注解绑定、消息解析、日志记录和常量引用
---

## 编写规范

### 1. 文件命名
- 文件名以 `MQListener` 结尾，或按业务功能命名
- 示例：`MQListener.java`、`AuthUserMQListener.java`、`TransportTaskMQListener.java`、`OrderMQListener.java`

### 2. 类命名
- 类名与文件名保持一致
- 使用大驼峰命名

### 3. 包结构规范
- 包名：`com.itheima.ms.<服务名>.mq`
- 示例：
  - `com.itheima.ms.oms.mq.MQListener`
  - `com.itheima.ms.work.mq.TransportTaskMQListener`
  - `com.itheima.ms.dispatch.mq.OrderMQListener`

### 4. 必需注解

#### 4.1 类级注解
```java
@Slf4j
@Component
public class TransportTaskMQListener {
```

| 注解 | 必需 | 说明 |
|------|------|------|
| `@Slf4j` | 是 | 日志记录 |
| `@Component` | 是 | Spring Bean 注册 |
| `@RequiredArgsConstructor` | 可选 | 构造器注入（推荐） |

#### 4.2 方法级注解 — `@RabbitListener`

**标准绑定格式：**
```java
@RabbitListener(bindings = @QueueBinding(
        value = @Queue(name = Constants.MQ.Queues.WORK_TRANSPORT_TASK_CREATE),
        exchange = @Exchange(name = Constants.MQ.Exchanges.TRANSPORT_TASK, type = ExchangeTypes.TOPIC),
        key = Constants.MQ.RoutingKeys.TRANSPORT_TASK_CREATE
))
public void listenTransportTaskMsg(String msg) {
```

**延迟队列格式（需添加 `delayed` 属性）：**
```java
@RabbitListener(bindings = @QueueBinding(
        value = @Queue(name = Constants.MQ.Queues.DISPATCH_ORDER_TO_PICKUP_DISPATCH_TASK),
        exchange = @Exchange(name = Constants.MQ.Exchanges.ORDER_DELAYED, type = ExchangeTypes.TOPIC, delayed = Constants.MQ.DELAYED),
        key = Constants.MQ.RoutingKeys.ORDER_CREATE
))
public void listenOrderMsg(String msg) {
```

| 属性 | 说明 |
|------|------|
| `value` | `@Queue` 定义队列名，引用 `Constants.MQ.Queues` |
| `exchange` | `@Exchange` 定义交换机，引用 `Constants.MQ.Exchanges` |
| `key` | 路由键，引用 `Constants.MQ.RoutingKeys` |
| `delayed` | 延迟队列设为 `Constants.MQ.DELAYED`（值为 `"true"`） |

### 5. 方法签名规范

**参数必须为 `String msg`**（原始 JSON 字符串）：
```java
public void listenXxxMsg(String msg) {
```

**方法命名：**
- 以 `listen` 开头，描述监听的消息类型
- 示例：`listenTransportTaskMsg`、`listenOrderMsg`、`listenTradeUpdatePayStatusMsg`

### 6. 消息解析规范

使用 Hutool 的 `JSONUtil` 解析消息：

```java
// 单个对象
TransportTaskCreateMsgDTO msgDTO = JSONUtil.toBean(msg, TransportTaskCreateMsgDTO.class);

// 列表
List<TradeStatusMsg> msgList = JSONUtil.toBean(msg, new TypeReference<List<TradeStatusMsg>>() {}, false);

// JSON 对象（灵活解析）
JSONObject jsonObject = JSONUtil.parseObj(msg);
String type = jsonObject.getStr("type");
```

### 7. 日志规范

**方法第一行必须记录日志：**
```java
log.info("接收到订单的消息 >>> msg = {}", msg);

// 或带队列名
log.info("接收到更新运单状态的消息 ({})-> {}", Constants.MQ.Queues.OMS_TRANSPORT_ORDER_UPDATE_STATUS, msg);
```

### 8. 常量引用规范

**所有 MQ 常量统一从 `Constants.MQ` 获取：**

```java
import com.itheima.chaincloud.common.constant.Constants;

// 交换机
Constants.MQ.Exchanges.ORDER_DELAYED          // "cc.exchange.topic.order.delayed"
Constants.MQ.Exchanges.TRANSPORT_TASK         // "cc.exchange.topic.transportTask"

// 队列
Constants.MQ.Queues.WORK_TRANSPORT_TASK_CREATE  // "cc.queue.work.transportTask.create"
Constants.MQ.Queues.DISPATCH_ORDER_TO_PICKUP_DISPATCH_TASK

// 路由键
Constants.MQ.RoutingKeys.CREATE               // "CREATE"
Constants.MQ.RoutingKeys.ORDER_CREATE         // "CREATE"

// 延迟
Constants.MQ.DELAYED      // "true"
Constants.MQ.DEFAULT_DELAY // -1
Constants.MQ.LOW_DELAY     // 5000 (5秒)
Constants.MQ.NORMAL_DELAY  // 10000 (10秒)
```

**命名约定：**

| 类型 | 命名格式 | 示例 |
|------|---------|------|
| 交换机 | `cc.exchange.topic.<业务域>[.delayed]` | `cc.exchange.topic.order.delayed` |
| 队列 | `cc.queue.<服务名>.<业务域>.<动作>` | `cc.queue.work.transportTask.create` |
| 路由键 | 大写动作词 | `CREATE`、`UPDATE`、`COMPLETE` |

### 9. 依赖注入

**方式1：`@Resource`（传统方式）**
```java
@Resource
private TransportTaskService transportTaskService;
```

**方式2：`@RequiredArgsConstructor`（推荐）**
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class TransportTaskMQListener {
    private final TransportTaskService transportTaskService;
    private final DriverJobFeign driverJobFeign;
}
```

### 10. 导入语句

```java
// Hutool JSON
import cn.hutool.json.JSONUtil;

// RabbitMQ
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

// 常量
import com.itheima.chaincloud.common.constant.Constants;

// Spring
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
```

### 11. 完整示例

```java
package com.itheima.ms.work.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.api.driver.DriverJobFeign;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.ms.work.mq.bean.TransportTaskCreateMsgDTO;
import com.itheima.ms.work.service.TransportTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransportTaskMQListener {

    private final DriverJobFeign driverJobFeign;
    private final TransportTaskService transportTaskService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.WORK_TRANSPORT_TASK_CREATE),
            exchange = @Exchange(name = Constants.MQ.Exchanges.TRANSPORT_TASK, type = ExchangeTypes.TOPIC),
            key = Constants.MQ.RoutingKeys.TRANSPORT_TASK_CREATE
    ))
    public void listenTransportTaskMsg(String msg) {
        log.info("接收到创建运输任务的消息 >>> msg = {}", msg);
        var transportTaskMsg = JSONUtil.toBean(msg, TransportTaskCreateMsgDTO.class);
        var driverIds = transportTaskMsg.getDriverIds();

        // 业务处理
        var transportTaskId = this.createTransportTask(transportTaskMsg);

        if (CollUtil.isEmpty(driverIds)) {
            log.info("司机列表为空，需要手动分配 -> msg = {}", msg);
            return;
        }

        driverIds.forEach(driverId -> this.driverJobFeign.createDriverJob(transportTaskId, driverId));
    }
}
```

**错误示例：**
```java
// 错误1：参数类型不是 String
public void listenMsg(OrderDTO dto) {  // 应为 String msg，再手动解析
}

// 错误2：硬编码队列名（应使用常量）
@RabbitListener(bindings = @QueueBinding(
        value = @Queue(name = "cc.queue.work.transportTask.create"),  // 应使用 Constants.MQ.Queues.XXX
        // ...
))

// 错误3：缺少日志记录
public void listenMsg(String msg) {
    // 缺少 log.info 记录接收到的消息
    var dto = JSONUtil.toBean(msg, XxxDTO.class);
}

// 错误4：延迟队列忘记设置 delayed 属性
@Exchange(name = Constants.MQ.Exchanges.ORDER_DELAYED, type = ExchangeTypes.TOPIC)
// 缺少 delayed = Constants.MQ.DELAYED
```

### 12. 注意事项

- **消息参数始终是 `String`**：手动用 `JSONUtil` 解析，不依赖自动反序列化
- **先记日志再处理**：方便排查消息消费问题
- **空值防御**：解析后检查关键字段是否为空，提前 return
- **异常处理**：MQ 框架自带重试机制，一般不需要 try-catch；如需特殊处理可添加
- **事务控制**：如需事务，在方法上添加 `@Transactional`
- **新增常量时**：在 `Constants.MQ` 的 `Exchanges`、`Queues`、`RoutingKeys` 接口中按命名约定添加
