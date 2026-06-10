# AGENTS.md - chain-cloud (智链云途) 项目指南

## 技术栈

- **Java 17** + **Spring Boot 2.7.12** + **Spring Cloud 2021.0.3** + **Spring Cloud Alibaba 2021.0.4.0**
- **Maven** 多模块聚合工程
- 关键依赖：MyBatis-Plus 3.5.5、Redisson 3.27.0、Nacos 配置、Seata 1.7.1 分布式事务、XXL-Job 2.4.1 定时任务、Hutool 5.8.0.M3、Knife4j 3.0.3、MySQL 8.0.33、Caffeine 3.1.8、Aliyun OSS 3.17.4、Itcast Auth 1.0.8、Netty 4.1.107.Final

## 项目结构

| 模块 | 说明 | 类型 |
|------|------|------|
| `chain-cloud-common` | 通用工具、注解、异常、配置、拦截器、VO 等 | 纯依赖库 |
| `chain-cloud-ms-domain` | 领域模型（各业务域 DTO、枚举、常量） | 纯依赖库 |
| `chain-cloud-ms-api` | Feign 接口定义（含 Fallback） | 纯依赖库 |
| `chain-cloud-mq` | RabbitMQ 消息队列封装（发送监控、重试、失败持久化） | 纯依赖库 |
| `chain-cloud-gateway` | Spring Cloud Gateway 网关（鉴权过滤器、跨域配置） | 可启动 |
| `chain-cloud-ms-base-service` | 基础数据服务（区域、货物类型、车辆、司机、排班、消息） | 可启动 |
| `chain-cloud-ms-auth-service` | 认证服务（登录、登录记录） | 可启动 |
| `chain-cloud-ms-user-service` | 用户服务（会员、地址簿） | 可启动 |
| `chain-cloud-ms-oms-service` | 订单管理服务（订单、货物、热门商品） | 可启动 |
| `chain-cloud-ms-work-service` | 作业服务（运输任务、运输订单、揽收调度） | 可启动 |
| `chain-cloud-ms-dispatch-service` | 调度服务（智能调度、ML 模型、定时任务） | 可启动 |
| `chain-cloud-ms-transport-service` | 运输服务（运输线路、成本配置、调度配置、网点） | 可启动 |
| `chain-cloud-ms-transport-info-service` | 运输信息服务（多级缓存、布隆过滤器） | 可启动 |
| `chain-cloud-ms-trade-service` | 交易服务（微信支付、支付宝、退款、通知） | 可启动 |
| `chain-cloud-ms-carriage-service` | 运费服务 | 可启动 |
| `chain-cloud-ms-courier-service` | 快递员服务 | 可启动 |
| `chain-cloud-ms-driver-service` | 司机服务 | 可启动 |
| `chain-cloud-ms-track-service` | 轨迹服务（物流轨迹） | 可启动 |
| `chain-cloud-ms-sms-service` | 短信服务（阿里云短信、多渠道路由） | 可启动 |
| `chain-cloud-ms-search-service` | 搜索服务（ElasticSearch） | 可启动 |
| `chain-cloud-ms-service-scope-service` | 服务范围服务 | 可启动 |
| `chain-cloud-ms-aigc-service` | AIGC 智能服务（AI 调度、风控、成本因子、物品审核） | 可启动 |
| `chain-cloud-ms-web-manager` | 管理端后台接口（后台管理系统） | 可启动 |
| `chain-cloud-ms-web-customer` | 客户/货主端接口 | 可启动 |
| `chain-cloud-ms-web-courier` | 快递员端接口 | 可启动 |
| `chain-cloud-ms-web-driver` | 司机端接口 | 可启动 |

## 模块详情

### chain-cloud-common

基础包：`com.itheima.chaincloud.common`

| 包 | 内容 |
|----|------|
| annotation/ | `@NoAuthorization` 自定义注解 |
| aspect/ | `ValidatedAspect` 参数校验切面 |
| config/ | `Knife4jConfiguration`、`MybatisPlusConfig`、`WebConfig`、`CommonFeignErrorDecoder`、`FeignErrorDecoder` |
| constant/ | `Constants` |
| entity/ | `BaseEntity` 基础实体 |
| enums/ | `BaseEnum`、`IdEnum`、`BaseExceptionEnum` |
| exception/ | `CCException`、`CCWebException` |
| handler/ | `GlobalExceptionHandler`、`ConvertHandler`、`MyMetaObjectHandler` |
| interceptor/ | `UserFeignRequestInterceptor`、`UserInterceptor`、`TokenInterceptor` |
| properties/ | `SwaggerConfigProperties`、`RealNameVerifyProperties`、`AliOSSProperties` |
| service/ | `AliOssService`、`IdService`、`RealNameVerifyService` |
| util/ | `JwtUtils`、`DateUtils`、`HttpUtil`、`BeanUtil`、`UserThreadLocal`、`AuthTemplateThreadLocal`、`PageResponse`、`CoordinateUtil`、`ObjectUtil`、`LocationUtils`、`StrUtil`、`ResponseEntityUtils`、`AspectUtil` |
| vo/ | `R`、`AuthUserInfo`、`BaseMsg`、`OrderMsg`、`TradeStatusMsg`、`CourierMsg`、`CourierTaskMsg`、`TransportOrderStatusMsg`、`TransportInfoMsg`、`TransportOrderMsg` |

### chain-cloud-ms-domain

基础包：`com.itheima.chaincloud.domain`，按业务域组织：

| 子包 | 主要内容 |
|------|---------|
| base/basic/ | `AreaDTO`、`GoodsTypeDTO`、`MessageDTO`、`WorkPatternDTO`、`WorkSchedulingDTO`、`MessageQueryDTO`、`MessageAddDTO` 等 |
| base/enums/ | `StatusEnum`、`TruckPlanStatusEnum`、`MessageBussinessTypeEnum`、`WorkUserTypeEnum`、`WorkStatusEnum`、`WorkPatternEnum`、`TruckRunStatusEnum`、`TruckWorkStatusEnum`、`TruckPlanScheduleStatusEnum`、`TruckTripsPeriodEnum`、`TruckAccidentTypeEnum`、`MessageContentTypeEnum`、`TruckFaultTypeEnum`、`TruckBreakRulesTypeEnum` |
| base/truck/ | `TruckDTO`、`TruckPlanDTO`、`TruckTripsDTO`、`TruckTypeDTO`、`TruckLicenseDTO`、`TruckReturnRegisterDTO`、`TransportTripsTruckDriverDTO` 等 |
| base/user/ | `TruckDriverDTO`、`TruckDriverLicenseDTO` |
| transport/domain/ | `CostConfigurationDTO`、`OrganDTO`、`TransportLineDTO`、`TransportLineNodeDTO`、`TransportLineSearchDTO`、`DispatchConfigurationDTO` |
| transport/enums/ | `DispatchMethodEnum` |
| aigc/domain/ | `TransportTaskDispatchResponse`、`TransportOrderDispatchResponse`、`ServiceScopeResponse`、`RiskLoginResponse`、`ParcelItemCheckResponse`、`ComputeCarriageResponse` 等 |
| aigc/enums/ | `RiskLevel` |
| auth/domain/ | `LoginRecordDTO` |
| carriage/domain/ | `WaybillDTO`、`CarriageDTO` |
| carriage/enums/ | `EconomicRegionEnum` |
| carriage/constant/ | `CarriageConstant` |
| courier/domain/ | `TaskPickupDispatchDTO`、`CourierScopeDTO`、`CourierStatusDTO` |
| driver/domain/ | `DriverJobDTO`、`DriverJobStatisticsDTO`、`DailyMileageDTO`、`DriverReturnRegisterDTO`、`DriverPickUpDTO`、`DriverDeliverDTO` 等 |
| driver/enums/ | `DriverJobStatusEnum` |
| oms/domain/ | `OrderDTO`、`OrderDetailDTO`、`OrderCargoDTO`、`OrderCarriageDTO`、`OrderSearchDTO`、`OrderStatusCountDTO`、`OrderPickupDTO`、`OrderLocationDTO`、`MailingSaveDTO` |
| oms/enums/ | `OrderTypeEnum`、`OrderStatusEnum`、`OrderPickupTypeEnum`、`OrderPaymentStatusEnum`、`OrderPaymentMethodEnum`、`MailTypeEnum` |
| scope/domain/ | `ServiceScopeDTO`、`Coordinate` |
| search/domain/ | `CourierTaskDTO`、`CourierTaskPageQueryDTO` |
| sms/domain/ | `SmsInfoDTO`、`SendResultDTO` |
| sms/enums/ | `SendStatusEnum`、`SmsContentTypeEnum`、`SmsTypeEnum` |
| track/domain/ | `TrackDTO`、`MarkerPointDTO` |
| track/enums/ | `TrackStatusEnum`、`TrackTypeEnum` |
| trade/domain/ | `TradingDTO`、`PayChannelDTO`、`RefundRecordDTO`、`NativePayDTO`/`NativePayResponseDTO`、`JsapiPayDTO`/`JsapiPayResponseDTO` |
| trade/enums/ | `TradingStateEnum`、`RefundStatusEnum`、`PayChannelEnum` |
| transportInfo/domain/ | `TransportInfoDTO`、`TransportInfoDetailDTO` |
| user/domain/ | `MemberDTO`、`AddressBookDTO` |
| user/enums/ | `MemberIdCardVerifyStatusEnum`、`AddressBookTypeEnum`、`AddressBookShowStatusEnum` |
| work/domain/ | `TransportTaskDTO`、`TransportOrderDTO`、`PickupDispatchTaskDTO`、`TaskStatsDTO`、`CourierTaskCountDTO` 等 |
| work/enums/ | `TransportTaskStatusEnum`、`TransportOrderStatusEnum`、`PickupDispatchTaskStatusEnum`、`PickupDispatchTaskTypeEnum`、`SignRecipientEnum` 等 |

### chain-cloud-ms-api

基础包：`com.itheima.chaincloud.api`，按业务域组织：

| 子包 | Feign 接口 |
|------|-----------|
| base/common/ | `AreaFeign`、`AuthFeign`、`GoodsTypeFeign`、`MessageFeign`、`MQFeign`、`WorkPatternFeign`、`WorkSchedulingFeign` |
| base/trips/ | `TripsFeign` |
| base/truck/ | `TruckFeign`、`TruckPlanFeign`、`TruckLicenseFeign`、`TruckReturnRegisterFeign`、`TruckTypeFeign` |
| base/user/ | `DriverFeign` |
| transport/ | `CostConfigurationFeign`、`OrganFeign`、`TransportLineFeign` |
| aigc/ | `AIGCFeign` |
| auth/ | `AuthFeign` |
| carriage/ | `CarriageFeign` |
| courier/ | `CourierFeign` |
| driver/ | `DriverJobFeign` |
| oms/ | `OrderFeign`、`GoodFeign`、`CargoFeign` |
| scope/ | `ServiceScopeFeign` |
| search/ | `CourierTaskFeign` |
| sms/ | `SmsFeign` |
| track/ | `TrackFeign` |
| trade/ | `TradingFeign`、`NativePayFeign`、`JsapiPayFeign`、`RefundRecordFeign`（含 FallbackFactory） |
| transportInfo/ | `TransportInfoFeign`（含 FallbackFactory） |
| user/ | `MemberFeign`、`AddressBookFeign` |
| work/ | `TransportTaskFeign`、`TransportOrderFeign`、`PickupDispatchTaskFeign` |

### chain-cloud-gateway

基础包：`com.itheima.gateway`

| 包 | 内容 |
|----|------|
| config/ | `CorsConfig`、`MyConfig` |
| filter/ | `AuthFilter`、`TokenGatewayFilter`、`ManagerTokenGatewayFilterFactory`、`DriverTokenGatewayFilterFactory`、`CustomerTokenGatewayFilterFactory`、`CourierTokenGatewayFilterFactory` |
| properties/ | `JwtProperties` |

### chain-cloud-mq

基础包：`com.itheima.chaincloud.mq`

| 包 | 内容 |
|----|------|
| config/ | `MessageConfig`、`ErrorMessageConfig` |
| entity/ | `FailMsgEntity` |
| exception/ | `MsgException` |
| job/ | `FailMsgJob`（XXL-Job 重试） |
| mapper/ | `FailMsgMapper` |
| service/ | `MQService`、`FailMsgService` 及实现 |

### 业务微服务模块

每个业务微服务均遵循标准分层：`controller/` → `service/` → `mapper/` → `entity/`，部分模块含 `config/`、`mq/`、`job/`、`enums/`、`utils/` 等。

**chain-cloud-ms-base-service**（`com.itheima.ms.base`）
- controller/base/：区域、货物类型、工作模式、排班、消息、`AuthController`、`MQController`
- controller/truck/：车辆、车辆计划、趟次、回车登记、车辆类型
- controller/user/：司机
- service/：`AuthService`、`WorkHistorySchedulingService`、`TruckPlanCreateService`、`TransportTripsTruckDriverService`、`TruckTypeGoodsTypeService` 等
- mq/：`AuthUserMQListener`、`TruckPlanScheduledMQListener`
- job/：`HistoryScheduleJob`
- utils/：`WorkPatternUtils`、`WorkSchedulingUtils`

**chain-cloud-ms-auth-service**（`com.itheima.ms.auth`）
- controller/：`AuthController`、`LoginRecordController`
- service/：`AuthService`、`LoginRecordService`
- config/：`AsyncConfig`

**chain-cloud-ms-user-service**（`com.itheima.ms.user`）
- controller/：`MemberController`、`AddressBookController`

**chain-cloud-ms-oms-service**（`com.itheima.ms.oms`）
- controller/：`OrderController`、`CargoController`、`HotGoodController`
- service/：`OrderService`、`OrderCargoService`、`OrderLocationService`、`CrudOrderService`、`HotGoodService`
- mq/：`MQListener`

**chain-cloud-ms-transport-service**（`com.itheima.ms.transport`）
- controller/：`TransportLineController`、`CostConfigurationController`、`DispatchConfigurationController`、`OrganController`
- repository/：`TransportLineRepository`、`OrganRepository`、`TLTRepository`、`OLTRepository`、`AgencyRepository`
- service/：`TLTService`、`OLTService`、`AgencyService`
- mq/：`AuthMQListener`
- utils/：`TransportLineUtils`、`DistanceCalculator`、`OrganServiceFactory`、`OrganUtils`

**chain-cloud-ms-transport-info-service**（`com.itheima.ms.transportinfo`）
- controller/：`TransportInfoController`
- config/：`RedissonConfiguration`、`RedisConfig`、`MyRedisCacheManager`、`CaffeineConfig`、`BloomFilterConfig`
- mq/：`TransportInfoMQListener`、`RedisMessageListener`
- service/：`TransportInfoService`、`BloomFilterService`

**chain-cloud-ms-work-service**（`com.itheima.ms.work`）
- controller/：`TransportTaskController`、`TransportOrderController`、`PickupDispatchTaskController`
- mq/：`TransportTaskMQListener`、`CourierMQListener`
- service/：`TransportTaskService`、`TransportOrderService`、`TransportOrderTaskService`、`PickupDispatchTaskService`

**chain-cloud-ms-trade-service**（`com.itheima.ms.trade`）
- controller/：`TradingController`、`PayChannelController`、`NativePayController`、`JsapiPayController`、`BasicPayController`、`NotifyController`、`RefundRecordController`
- handler/：`HandlerFactory`、`BasicPayHandler`、`BeforePayHandler`、`NativePayHandler`、`JsapiPayHandler`
- handler/wechat/：`WechatNativePayHandler`、`WechatJsapiPayHandler`、`WeChatBasicPayHandler`
- handler/alipay/：`AliNativePayHandler`、`AliBasicPayHandler`
- annotation/：`@PayChannel`
- job/：`TradeJob`

**chain-cloud-ms-carriage-service**（`com.itheima.ms.carriage`）
- controller/：`CarriageController`
- utils/：`CarriageUtils`

**chain-cloud-ms-courier-service**（`com.itheima.ms.courier`）
- controller/：`CourierController`
- service/：`CourierUserService`、`CourierStatusService`

**chain-cloud-ms-driver-service**（`com.itheima.ms.driver`）
- controller/：`DriverJobController`

**chain-cloud-ms-track-service**（`com.itheima.ms.track`）
- controller/：`TrackController`
- mq/：`MQListener`
- util/：`TrackEntityUtil`

**chain-cloud-ms-sms-service**（`com.itheima.ms.sms`）
- controller/：`SmsController`
- handler/：`HandlerFactory`、`SmsSendHandler`、`AliYunSmsSendHandler`
- annotation/：`@SendChannel`
- service/：`SmsService`、`RouteService`

**chain-cloud-ms-search-service**（`com.itheima.ms.search`）
- controller/：`CourierTaskController`
- config/：`ElasticSearchConfig`、`LocalDateTimeSerializingConfig`
- mq/：`CourierTaskMQListener`

**chain-cloud-ms-service-scope-service**（`com.itheima.ms.scope`）
- controller/：`ScopeController`
- util/：`EntityUtils`

**chain-cloud-ms-dispatch-service**（`com.itheima.ms.dispatch`）
- config/：`XxlJobConfig`、`RedissonConfiguration`
- job/：`DispatchJob`、`DispatchContext`
- ml/model/：`TruckInfo`、`FeatureSnapshot`、`DispatchResult`、`DispatchDecisionRecord`
- ml/model/enums/：`DispatchStatus`
- mq/：`TransportOrderDispatchMQListener`、`OrderMQListener`

**chain-cloud-ms-aigc-service**（`com.itheima.ms.aigc`）
- controller/：`ServiceScopeController`、`RiskControlController`、`TransportOrderDispatchController`、`OrganTypeAgentController`、`TransportTaskDispatchController`、`CostFactorController`、`ParcelItemCheckController`、`CourierDispatchController`、`ComputeCarriageAgentController`
- feign/：`AgentServerClient`
- service/：各 Controller 对应的 Service 接口及实现

### Web 接口模块

每个 Web 模块均包含 `controller/`、`service/`、`vo/`、`config/` 层，`config/` 中均有 `WebFeignErrorDecoder` 和各模块专属配置。

**chain-cloud-ms-web-manager**（`com.itheima.ms.web.manager`）
- controller/auth/：`AuthController`
- controller/agency/：`AgencyController`
- controller/base/：区域、货物类型、工作模式、排班、工作空间、文件上传
- controller/truck/：车辆、趟次、回车登记、车辆类型、司机
- controller/carriage/：`CarriageController`
- controller/oms/：订单、货物
- controller/transport/：运输线路、调度配置、成本配置
- controller/work/：运输任务、运输订单、揽收调度
- util/：`ExcelUtil`、`ImportVOListener`、`RowFormatSetTextHandler`

**chain-cloud-ms-web-customer**（`com.itheima.ms.web.customer`）
- controller/：`AreaController`、`AddressBookController`、`CargoController`、`CarriageController`、`OrderController`、`UserController`
- service/：`MemberService`、`BaseService`、`WechatService`、`TokenService`、`OmsService`、`CarriageService`、`AddressBookService`

**chain-cloud-ms-web-courier**（`com.itheima.ms.web.courier`）
- controller/：`LoginController`、`UserController`、`TaskController`、`TrackController`、`MessageController`、`AreaController`、`PayController`

**chain-cloud-ms-web-driver**（`com.itheima.ms.web.driver`）
- controller/：`LoginController`、`UserController`、`TaskController`、`TrackController`、`MessageController`、`FileUploadController`
- config/：`OSSConfiguration`、`LoadBalanceConfig`

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

| 依赖 | 版本         |
|------|------------|
| Java | 17         |
| Spring Boot | 2.7.12     |
| Spring Cloud | 2021.0.3   |
| Spring Cloud Alibaba | 2021.0.4.0 |
| MyBatis-Plus | 3.5.5      |
| MySQL | 8.0.33     |
| Redisson | 3.27.0     |
| Seata | 1.7.1      |
| XXL-Job | 2.4.1      |
| Hutool | 5.8.0.M3   |
| Knife4j | 3.0.3      |
| Caffeine | 3.1.8      |
| Aliyun OSS | 3.17.4     |
| JJWT | 0.11.5     |
| Itcast Auth | 1.0.8      |
| Lombok | 1.18.30    |
| AspectJ | 1.9.21     |
| HikariCP | 5.1.0      |
| OkHttp | 4.12.0     |
| Nacos Config | 0.2.10     |
| EagleMap | 1.0        |
| Netty | 4.1.107.Final |

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
| karpathy-guidelines | 减少常见 LLM 编码错误的行为准则，用于编写、审查或重构代码时避免过度复杂化 |
| plantuml | 定义项目中 PlantUML 图代码生成规范，包括时序图、流程图等图表的编写规范和最佳实践 |

调用方式：在需要时使用 `/skill domain-dto` 或 `/skill domain-enum` 或 `/skill api-feign` 等加载技能指导
