# AGENTS.md - chain-cloud (智链云途) 项目指南

## 技术栈

- **Java 11** + **Spring Boot 2.7.12** + **Spring Cloud 2021.0.3** + **Spring Cloud Alibaba 2021.0.4.0**
- **Maven** 多模块聚合工程
- 关键依赖：MyBatis-Plus 3.5.2、Redisson 3.17.1、Nacos 配置、Seata 1.5.2 分布式事务、XXL-Job 2.3.0 定时任务、Hutool 5.8.44、Knife4j 3.0.3、MySQL 8.0.28、Caffeine 3.1.1、Aliyun OSS 3.10.2、Itcast Auth 1.0.8

## 项目结构

```
chain-cloud/ (根聚合pom)
├── chain-cloud-common/                -- 通用模块（纯依赖库，无启动类）
│   ├── annotation/                    -- 自定义注解（@NoAuthorization 等）
│   ├── aspect/                        -- 切面
│   ├── config/                        -- 通用配置（Knife4jConfiguration 等）
│   ├── constant/                      -- 常量
│   ├── entity/                        -- 通用实体
│   ├── enums/                         -- 基础枚举（含 BaseEnum 接口）
│   ├── exception/                     -- 异常（CCException 等）
│   ├── handler/                       -- 全局异常处理器（GlobalExceptionHandler）
│   ├── interceptor/                   -- 拦截器（UserFeignRequestInterceptor 等）
│   ├── properties/                    -- 属性绑定
│   ├── service/                       -- 通用服务（AliOssService, IdService, RealNameVerifyService）
│   ├── util/                          -- 工具类（JwtUtils, DateUtils, HttpUtil, BeanUtil 等）
│   └── vo/                            -- 值对象（R, AuthUserInfo, BaseMsg, OrderMsg 等）
│
├── chain-cloud-ms-domain/             -- 领域模型层（DTO、枚举、常量，不含实体和事件）
│   ├── base/
│   │   ├── basic/                     -- 基础 DTO（AreaDTO, GoodsTypeDTO, MessageDTO, WorkPatternDTO 等）
│   │   ├── constants/                 -- 常量类（MessageConstants, TruckConstant）
│   │   ├── enums/                     -- 枚举（StatusEnum, TruckPlanStatusEnum, MessageBussinessTypeEnum 等）
│   │   ├── truck/                     -- 车辆 DTO（TruckDTO, TruckPlanDTO, TruckTripsDTO 等）
│   │   └── user/                      -- 用户 DTO（TruckDriverDTO, TruckDriverLicenseDTO）
│   └── transport/
│       ├── domain/                    -- 运输 DTO（CostConfigurationDTO, OrganDTO, TransportLineDTO 等）
│       └── enums/                     -- 运输枚举（DispatchMethodEnum）
│
├── chain-cloud-ms-api/                -- API 层（Feign 接口定义）
│   ├── base/
│   │   ├── common/                    -- 基础 Feign（AreaFeign, AuthFeign, GoodsTypeFeign, MessageFeign, MQFeign, WorkPatternFeign 等）
│   │   ├── trips/                     -- 趟次 Feign（TripsFeign）
│   │   ├── truck/                     -- 车辆 Feign（TruckFeign, TruckPlanFeign, TruckLicenseFeign, TruckReturnRegisterFeign 等）
│   │   └── user/                      -- 用户 Feign（DriverFeign）
│   └── transport/                     -- 运输 Feign（CostConfigurationFeign, OrganFeign, TransportLineFeign 等）
│
├── chain-cloud-gateway/               -- API 网关模块（Spring Cloud Gateway）
│   └── src/main/java/com/itheima/
│       └── GatewayApplication.java    -- 网关启动类
│
├── chain-cloud-mq/                    -- 消息队列模块（RabbitMQ 封装）
│   ├── config/                        -- MessageConfig, ErrorMessageConfig
│   ├── entity/                        -- FailMsgEntity
│   ├── exception/                     -- MsgException
│   ├── job/                           -- FailMsgJob（XXL-Job 重试）
│   ├── mapper/                        -- FailMsgMapper
│   └── service/                       -- MQService, FailMsgService 及实现
│
├── chain-cloud-ms-base-service/       -- 基础微服务（区域、货物类型、车辆、司机、排班等基础数据）
│   └── src/main/java/com/itheima/ms/base/
│       ├── controller/                -- 控制器（base, truck, user 分组）
│       ├── entity/                    -- 持久化实体（Area, Truck, TruckPlan, TruckDriver 等）
│       ├── service/                   -- 服务层接口与实现
│       ├── mapper/                    -- MyBatis-Plus Mapper 接口
│       ├── job/                       -- XXL-Job 定时任务（HistoryScheduleJob）
│       ├── mq/                        -- MQ 监听器（AuthUserMQListener, TruckPlanScheduledMQListener）
│       └── config/                    -- XxlJobConfig
│
├── chain-cloud-ms-auth-service/       -- 认证微服务（登录、登录记录）
│   └── src/main/java/com/itheima/ms/auth/
│       └── controller/                -- AuthController, LoginRecordController
│
├── chain-cloud-ms-user-service/       -- 用户微服务（会员、地址簿）
│   └── src/main/java/com/itheima/ms/user/
│       └── controller/                -- MemberController, AddressBookController
│
├── chain-cloud-ms-oms-service/        -- 订单管理微服务（OMS）（订单、货物、热门商品）
│   └── src/main/java/com/itheima/ms/oms/
│       └── controller/                -- OrderController, CargoController, HotGoodController
│
├── chain-cloud-ms-transport-service/  -- 运输微服务（运输线路、成本配置、调度配置、网点）
│   └── src/main/java/com/itheima/ms/transport/
│       └── controller/                -- TransportLineController, CostConfigurationController, DispatchConfigurationController, OrganController
│
├── chain-cloud-ms-transport-info-service/  -- 运输信息微服务
│   └── src/main/java/com/itheima/ms/transportinfo/
│       └── controller/                -- TransportInfoController
│
├── chain-cloud-ms-work-service/       -- 作业微服务（运输任务、运输订单、揽收调度任务）
│   └── src/main/java/com/itheima/ms/work/
│       └── controller/                -- TransportTaskController, TransportOrderController, PickupDispatchTaskController
│
├── chain-cloud-ms-trade-service/      -- 交易微服务（支付、退款、通知）
│   └── src/main/java/com/itheima/ms/trade/
│       └── controller/                -- TradingController, PayChannelController, NativePayController, JsapiPayController, BasicPayController, NotifyController, RefundRecordController
│
├── chain-cloud-ms-carriage-service/   -- 运费微服务
│   └── src/main/java/com/itheima/ms/carriage/
│       └── controller/                -- CarriageController
│
├── chain-cloud-ms-courier-service/    -- 快递员微服务
│   └── src/main/java/com/itheima/ms/courier/
│       └── controller/                -- CourierController
│
├── chain-cloud-ms-driver-service/     -- 司机微服务
│   └── src/main/java/com/itheima/ms/driver/
│       └── controller/                -- DriverJobController
│
├── chain-cloud-ms-track-service/      -- 轨迹微服务（物流轨迹）
│   └── src/main/java/com/itheima/ms/track/
│       └── controller/                -- TrackController
│
├── chain-cloud-ms-sms-service/        -- 短信微服务
│   └── src/main/java/com/itheima/ms/sms/
│       └── controller/                -- SmsController
│
├── chain-cloud-ms-search-service/     -- 搜索微服务
│   └── src/main/java/com/itheima/ms/search/
│       └── controller/                -- CourierTaskController
│
├── chain-cloud-ms-service-scope-service/  -- 服务范围微服务
│   └── src/main/java/com/itheima/ms/scope/
│       └── controller/                -- ScopeController
│
├── chain-cloud-ms-dispatch-service/   -- 调度微服务
│
├── chain-cloud-ms-aigc-service/       -- AIGC 智能微服务（AI 调度、风控、成本因子等）
│   └── src/main/java/com/itheima/ms/aigc/
│       └── controller/                -- ServiceScopeController, RiskControlController, TransportOrderDispatchController, OrganTypeAgentController, TransportTaskDispatchController, CostFactorController, ParcelItemCheckController, CourierDispatchController, ComputeCarriageAgentController
│
├── chain-cloud-ms-web-manager/        -- 管理端 Web 接口（后台管理系统）
│   └── src/main/java/com/itheima/ms/web/manager/controller/
│       ├── auth/                      -- 认证（AuthController）
│       ├── agency/                    -- 代理管理（AgencyController）
│       ├── base/                      -- 基础数据（Area, GoodType, WorkPattern, WorkScheduling, Workspace, FileUpload）
│       ├── truck/                     -- 车辆管理（Truck, TruckTrips, TruckReturnRegister, TruckType, Driver）
│       ├── carriage/                  -- 运费管理（CarriageController）
│       ├── oms/                       -- 订单管理（Order, Cargo）
│       ├── transport/                 -- 运输管理（TransportLine, DispatchConfiguration, CostConfiguration）
│       └── work/                      -- 作业管理（TransportTask, TransportOrder, PickupDispatchTask）
│
├── chain-cloud-ms-web-customer/       -- 客户端 Web 接口（客户/货主端）
│   └── src/main/java/com/itheima/ms/web/customer/controller/
│       ├── AreaController             -- 区域
│       ├── AddressBookController      -- 地址簿
│       ├── CargoController            -- 货物
│       ├── CarriageController         -- 运费
│       ├── OrderController            -- 订单
│       └── UserController             -- 用户
│
├── chain-cloud-ms-web-courier/        -- 快递员端 Web 接口
│   └── src/main/java/com/itheima/ms/web/courier/controller/
│       ├── LoginController            -- 登录
│       ├── UserController             -- 用户
│       ├── TaskController             -- 任务
│       ├── TrackController            -- 轨迹
│       ├── MessageController          -- 消息
│       ├── AreaController             -- 区域
│       └── PayController              -- 支付
│
└── chain-cloud-ms-web-driver/         -- 司机端 Web 接口
    └── src/main/java/com/itheima/ms/web/driver/controller/
        ├── LoginController            -- 登录
        ├── UserController             -- 用户
        ├── TaskController             -- 任务
        ├── TrackController            -- 轨迹
        ├── MessageController          -- 消息
        └── FileUploadController       -- 文件上传
```

## 模块分类

### 基础模块
| 模块名 | 说明 | 启动类 |
|--------|------|--------|
| chain-cloud-common | 通用工具、注解、异常、配置等 | 无 |
| chain-cloud-ms-domain | 领域模型（DTO、枚举、常量） | 无 |
| chain-cloud-ms-api | Feign 接口定义 | 无 |
| chain-cloud-mq | RabbitMQ 消息队列封装 | 无 |

### 网关模块
| 模块名 | 说明 | 启动类 | 端口 |
|--------|------|--------|------|
| chain-cloud-gateway | Spring Cloud Gateway 网关 | GatewayApplication | 9527 |

### 业务微服务模块
| 模块名 | 说明 | 启动类 | 端口 |
|--------|------|--------|------|
| chain-cloud-ms-base-service | 基础数据服务（区域、货物类型、车辆、司机、排班等） | BaseApplication | 18081 |
| chain-cloud-ms-oms-service | 订单管理服务（OMS）（订单、货物、热门商品） | OmsApplication | 18082 |
| chain-cloud-ms-auth-service | 认证服务（登录、登录记录） | AuthApplication | 18100 |
| chain-cloud-ms-user-service | 用户服务（会员、地址簿） | UserApplication | 18084 |
| chain-cloud-ms-work-service | 作业服务（运输任务、运输订单、揽收调度） | WorkApplication | 18085 |
| chain-cloud-ms-dispatch-service | 调度服务 | DispatchApplication | 18086 |
| chain-cloud-ms-transport-info-service | 运输信息服务 | TransportInfoApplication | 18087 |
| chain-cloud-ms-courier-service | 快递员服务 | CourierApplication | 18088 |
| chain-cloud-ms-service-scope-service | 服务范围服务 | ServiceScopeApplication | 18089 |
| chain-cloud-ms-trade-service | 交易服务（支付、退款、通知） | TradeApplication | 18079 |
| chain-cloud-ms-carriage-service | 运费服务 | CarriageApplication | 18094 |
| chain-cloud-ms-driver-service | 司机服务 | DriverApplication | 18098 |
| chain-cloud-ms-track-service | 轨迹服务 | TrackApplication | 18097 |
| chain-cloud-ms-sms-service | 短信服务 | SmsApplication | 18095 |
| chain-cloud-ms-search-service | 搜索服务 | SearchApplication | 18099 |
| chain-cloud-ms-transport-service | 运输服务（线路、成本配置、调度配置、网点） | TransportApplication | 18096 |
| chain-cloud-ms-aigc-service | AIGC 智能服务（AI 调度、风控、成本因子等） | AIGCApplication | 18200 |

### Web 接口模块（面向不同终端）
| 模块名 | 说明 | 启动类 | 端口 |
|--------|------|--------|------|
| chain-cloud-ms-web-manager | 管理端后台接口 | WebManagerApplication | 18093 |
| chain-cloud-ms-web-customer | 客户/货主端接口 | WebCustomerApplication | 18091 |
| chain-cloud-ms-web-courier | 快递员端接口 | WebCourierApplication | 18090 |
| chain-cloud-ms-web-driver | 司机端接口 | WebDriverApplication | 18092 |

## 依赖版本（根 pom properties）

| 依赖 | 版本 |
|------|------|
| Java | 11 |
| Spring Boot | 2.7.12 |
| Spring Cloud | 2021.0.3 |
| Spring Cloud Alibaba | 2021.0.4.0 |
| MyBatis-Plus | 3.5.2 |
| MySQL | 8.0.28 |
| Redisson | 3.17.1 |
| Seata | 1.5.2 |
| XXL-Job | 2.3.0 |
| Hutool | 5.8.0.M3 |
| Knife4j | 3.0.3 |
| Caffeine | 3.1.1 |
| Aliyun OSS | 3.10.2 |
| JJWT | 0.9.1 |
| Itcast Auth | 1.0.8 |
| Lombok | 1.18.22 |
| AspectJ | 1.9.7 |
| HikariCP | 4.0.3 |
| OkHttp | 3.13.1 |
| Nacos Config | 0.2.10 |
| EagleMap | 1.0 |
| CGLIB | 3.3.0 |

## 常用命令

```bash
# 构建（根目录执行，会构建所有模块）
mvn clean package -DskipTests

# 编译单个模块
mvn clean compile -pl chain-cloud-common

# 安装到本地仓库
mvn install -pl chain-cloud-common

# 安装所有模块到本地仓库
mvn clean install -DskipTests
```

## 注意事项

- 使用 **Lombok**，注意 IDE 需要安装 Lombok 插件
- 根 pom 的 `<packaging>pom</packaging>` 表示这是聚合工程，不打包 jar
- `chain-cloud-common`、`chain-cloud-ms-domain`、`chain-cloud-ms-api`、`chain-cloud-mq` 不含启动类，是纯依赖库模块
- 网关模块：`chain-cloud-gateway`（GatewayApplication）
- 业务微服务模块：各 service 模块均有独立启动类
- Web 接口模块：各 web 模块均有独立启动类，面向不同终端提供接口
- `itcast-auth-spring-boot-starter` 是内部认证 starter，Feign 调用时会通过 `UserFeignRequestInterceptor` 传递用户上下文
- 使用 `@NoAuthorization` 注解可跳过接口鉴权
- 全局异常统一由 `GlobalExceptionHandler` 处理，基础异常类型为 `CCException`
- Swagger 文档使用 Knife4j（3.0.3），配置类在 `Knife4jConfiguration`
- MQ 模块提供消息发送监控、消费重试、失败持久化能力，失败消息表为 `cc_fail_msg`
- `bootstrap-local.yml` 中共享配置包含：seata、rabbitmq、mysql、mybatis-plus、xxl-job、authority、redis
- 服务注册与配置中心使用 Nacos（地址：`192.168.150.101:8848`，命名空间：`ecae68ba-7b43-4473-a980-4ddeb6157bdc`）

## AI 编码约束

- **pom.xml 修改需确认**：禁止随意修改 pom.xml 文件。如果确实需要新增/修改/删除依赖或插件，必须经过人工确认后方可执行。
- **禁止随意删除代码**：不删除现有代码（除非有明确指令），优先在现有代码基础上做增量修改。
- **最小影响范围**：尽量以最小改动完成需求，不重构无关代码，不改动与任务无关的文件。
- **保留已有注释**：不对现有注释进行删改。如需补充注释，在已有注释基础上增量添加。

## Skills

项目技能配置存放在 `.claude/skills/` 目录下：

| 技能名称 | 说明 |
|---------|------|
| domain-dto | 定义项目中 DTO 类编写规范，包括命名规范和注解要求 |
| domain-enum | 定义项目中枚举类编写规范，包括命名规范、注解要求和实现模式 |
| api-feign | 定义项目中 Feign API 接口编写规范，包括命名规范、注解要求和实现模式 |

调用方式：在需要时使用 `/skill domain-dto` 或 `/skill domain-enum` 或 `/skill api-feign` 加载技能指导
