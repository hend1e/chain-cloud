# chain-cloud-ms-base-service

基础微服务模块，提供车队、运输、司机、调度等核心业务服务。

## 模块概述

`chain-cloud-ms-base-service` 是链云物流系统的核心基础服务模块，基于 Spring Boot 2.7.12 + Spring Cloud 2021.0.3 构建。该模块提供车辆管理、运输调度、司机管理、排班管理等基础服务，是整个物流业务的数据和服务中枢。

## 技术栈

- **Java 11**
- **Spring Boot** 2.7.12
- **Spring Cloud** 2021.0.3
- **Spring Cloud Alibaba** 2021.0.4.0
- **MyBatis-Plus** 3.5.2
- **Redisson** 3.17.1
- **XXL-Job** 定时任务
- **Seata** 分布式事务

## 项目结构

```
chain-cloud-ms-base-service/
├── src/main/java/com/itheima/
│   ├── BaseApplication.java                 # 启动类
│   ├── ms/base/
│   │   ├── controller/                       # REST 接口层
│   │   │   ├── base/                         # 基础业务接口
│   │   │   │   ├── AreaController.java       # 区域管理
│   │   │   │   ├── AuthController.java       # 认证接口
│   │   │   │   ├── GoodsTypeController.java  # 货物类型
│   │   │   │   ├── MessageController.java    # 消息中心
│   │   │   │   ├── MQController.java         # 消息队列管理
│   │   │   │   ├── WorkPatternController.java # 工作模式
│   │   │   │   └── WorkSchedulingController.java # 排班管理
│   │   │   ├── truck/                        # 车辆运输接口
│   │   │   │   ├── TruckController.java      # 车辆管理
│   │   │   │   ├── TruckPlanController.java  # 车辆计划
│   │   │   │   ├── TruckTripsController.java # 运输趟次
│   │   │   │   ├── TruckTypeController.java   # 车辆类型
│   │   │   │   ├── TruckLicenseController.java # 车辆证照
│   │   │   │   └── TruckReturnRegisterController.java # 回车登记
│   │   │   └── user/                         # 用户接口
│   │   │       └── TruckDriverController.java # 司机管理
│   │   ├── service/                          # 业务逻辑层
│   │   │   ├── base/                         # 基础服务
│   │   │   ├── truck/                        # 车辆运输服务
│   │   │   └── user/                         # 用户服务
│   │   ├── mapper/                           # 数据访问层
│   │   ├── entity/                           # 实体类
│   │   ├── mq/                               # 消息监听
│   │   ├── job/                              # 定时任务
│   │   ├── config/                           # 配置类
│   │   ├── constant/                         # 常量定义
│   │   └── utils/                            # 工具类
│   └── transport/common/                    # 公共工具
└── src/test/java/                            # 单元测试
```

## 核心功能

### 1. 车辆管理服务

| 服务 | 说明 |
|------|------|
| TruckService | 车辆基础信息管理 |
| TruckTypeService | 车辆类型管理 |
| TruckLicenseService | 车辆证照管理（行驶证、保险等） |
| TruckPlanService | 车辆计划调度 |
| TruckTripsService | 运输趟次管理 |
| TruckReturnRegisterService | 回车登记管理 |

**主要实体**：
- `TruckEntity` - 车辆基本信息
- `TruckTypeEntity` - 车辆类型（车型、载重）
- `TruckPlanEntity` - 车辆调度计划
- `TruckTripsEntity` - 运输趟次记录
- `TruckLicenseEntity` - 车辆证照信息

### 2. 司机管理服务

| 服务 | 说明 |
|------|------|
| TruckDriverService | 司机信息管理 |
| TruckDriverLicenseService | 司机驾驶证管理 |

**主要实体**：
- `TruckDriverEntity` - 司机基本信息
- `TruckDriverLicenseEntity` - 驾驶证信息

### 3. 调度排班服务

| 服务 | 说明 |
|------|------|
| WorkPatternService | 工作模式管理 |
| WorkSchedulingService | 排班调度管理 |
| WorkHistorySchedulingService | 历史排班记录 |

**主要实体**：
- `WorkPatternEntity` - 工作模式（上班/休息等）
- `WorkSchedulingEntity` - 排班计划
- `WorkHistorySchedulingEntity` - 历史排班

### 4. 基础数据服务

| 服务 | 说明 |
|------|------|
| AreaService | 区域管理（省市区） |
| GoodsTypeService | 货物类型管理 |
| MessageService | 消息中心 |

**主要实体**：
- `AreaEntity` - 区域信息
- `GoodsTypeEntity` - 货物类型
- `MessageEntity` - 消息记录

### 5. 消息队列服务

| 监听器 | 说明 |
|--------|------|
| AuthUserMQListener | 用户认证消息监听 |
| TruckPlanScheduledMQListener | 车辆计划调度消息监听 |

### 6. 定时任务

| 任务 | 说明 |
|------|------|
| HistoryScheduleJob | 历史排班生成任务（XXL-Job） |

## 启动配置

### 依赖服务

- **Nacos** - 配置中心 & 服务发现
- **MySQL** - 数据存储
- **Redis** - 缓存（Redisson）
- **Seata** - 分布式事务
- **XXL-Job** - 定时任务调度

### 启动类

```java
com.itheima.BaseApplication
```

## 常用操作

```bash
# 编译模块
mvn clean compile -pl chain-cloud-ms-base-service

# 打包
mvn clean package -DskipTests -pl chain-cloud-ms-base-service

# 安装到本地仓库
mvn install -pl chain-cloud-ms-base-service
```

## 模块依赖关系

```
chain-cloud-ms-base-service
    ├── chain-cloud-common      # 通用工具、配置、拦截器
    ├── chain-cloud-ms-api     # API接口定义、DTO
    └── chain-cloud-mq         # 消息队列功能
```

## 注意事项

- 使用 Lombok 简化实体类代码
- 全局异常由 `GlobalExceptionHandler` 处理
- 使用 `@NoAuthorization` 注解跳过接口鉴权
- Feign 调用通过 `UserFeignRequestInterceptor` 传递用户上下文