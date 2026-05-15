# chain-cloud-mq 使用手册

## 1、模块说明

`chain-cloud-mq` 是链cloud项目的消息队列模块，对 RabbitMQ 的使用进行了统一封装，将消息发送、消费者配置等代码抽取为公共组件，便于各业务服务复用。

## 2、主要功能

- **消息发送监控**：为 `RabbitTemplate` 配置 `ReturnsCallback`，记录消息发送到交换机但未到达队列的情况
- **消费重试机制**：统一消费者配置，消息处理失败时自动重试，3次重试均失败后发送至 `error.queue`，待人工处理
- **失败消息持久化**：发送消息失败时，将消息内容持久化到 MySQL 数据库，后续通过 XXL-Job 定时任务重试发送

## 3、数据库表结构

失败消息记录表（cc_fail_msg）：

```sql
CREATE TABLE `cc_fail_msg` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `msg_id` varchar(32) DEFAULT NULL COMMENT '消息ID',
  `exchange` varchar(100) DEFAULT NULL COMMENT '交换机',
  `routing_key` varchar(50) DEFAULT NULL COMMENT '路由键',
  `msg` text COMMENT '消息内容',
  `reason` text COMMENT '失败原因',
  `created` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_created` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失败消息记录表';
```

## 4、使用指南

### 4.1、引入依赖

在业务服务的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.chaincloud.mq</groupId>
    <artifactId>chain-cloud-mq</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 4.2、启用重试机制

在启动类上添加 `@EnableRetry` 注解：

```java
package com.chaincloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableFeignClients
@SpringBootApplication
public class DispatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(DispatchApplication.class, args);
    }

}
```

### 4.3、配置 RabbitMQ

在 `bootstrap.yml` 或 `application.yml` 中配置 RabbitMQ：

```yaml
spring:
  rabbitmq:
    host: ${rabbitmq.host}
    port: ${rabbitmq.port}
    username: ${rabbitmq.username}
    password: ${rabbitmq.password}
    virtual-host: ${rabbitmq.virtual-host}
    publisher-confirm-type: correlated
    publisher-returns: true
    template:
      mandatory: true
    listener:
      simple:
        acknowledge-mode: auto
        retry:
          enabled: true
          initial-interval: 1000
          multiplier: 1
          max-attempts: 3
          stateless: true
```

**配置说明：**

| 配置项 | 说明 |
|--------|------|
| `publisher-confirm-type` | 消息发送异步回调，记录发送结果 |
| `publisher-returns` | 开启发布返回功能，消息到达交换机但未到达队列时触发回调 |
| `template.mandatory` | 路由失败时策略，true 调用 ReturnCallback，false 丢弃消息 |
| `listener.simple.acknowledge-mode` | auto：自动确认，有异常返回 nack 回滚消息，无异常返回 ack |
| `listener.simple.retry.enabled` | 开启消费者失败重试 |
| `listener.simple.retry.max-attempts` | 最大重试次数 |
| `listener.simple.retry.stateless` | true：无状态；false：有状态（业务含事务时需改为 false）|

### 3.2、启动类增加@EnableRetry

例如：

~~~java
package com.itheima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry  //开启重试机制
@EnableFeignClients
@SpringBootApplication
public class DispatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(DispatchApplication.class, args);
    }

}

~~~

### 3.3、编写配置

在springboot的配置文件中bootstrap-*.yml修改配置：

~~~yaml
spring:
  rabbitmq: #mq的配置
    host: ${rabbitmq.host}
    port: ${rabbitmq.port}
    username: ${rabbitmq.username}
    password: ${rabbitmq.password}
    virtual-host: ${rabbitmq.virtual-host}
    publisher-confirm-type: correlated #发送消息的异步回调，记录消息是否发送成功
    publisher-returns: true #开启publish-return功能，消息到达交换机，但是没有到达对列表
    template:
      mandatory: true #消息路由失败时的策略, true: 调用ReturnCallback, false：丢弃消息
    listener:
      simple:
        acknowledge-mode: auto #，出现异常时返回nack，消息回滚到mq；没有异常，返回ack
        retry:
          enabled: true # 开启消费者失败重试
          initial-interval: 1000 # 初识的失败等待时长为1秒
          multiplier: 1 # 失败的等待时长倍数，下次等待时长 = multiplier * last-interval
          max-attempts: 3 # 最大重试次数
          stateless: true # true无状态；false有状态。如果业务中包含事务，这里改为false
~~~

### 4.4、发送消息

```java
@Resource
private MQService mqService;

// 发送消息
mqService.sendMsg(exchange, routingKey, message);
```

### 4.5、消费消息

消息消费使用 Spring Boot 标准注解写法：

```java
@Component
public class OrderMQListener {

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(name = "${rabbitmq.order.queue}"),
        exchange = @Exchange(name = "${rabbitmq.order.exchange}", type = ExchangeTypes.TOPIC),
        key = "#"
    ))
    public void listenOrderMsg(String msg) {
        // 业务逻辑处理
    }
}
```

> **注意**：消费者重试失败后，消息会自动转发至 `error.queue`，需人工介入处理或通过 XXL-Job 定时任务重试。