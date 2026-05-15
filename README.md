# 智链云途 (Chain-Cloud) - AI 智能物流平台

> 基于 Spring Cloud Alibaba 微服务架构的生产级物流系统，深度融合 AI 技术实现从**规则驱动**到**智能驱动**的全面升级。

## 一、项目简介

**智链云途 AI 版** 是一个基于 Spring Cloud Alibaba 微服务架构体系的【生产级】物流项目系统，在完整保留原有微服务工程实践的基础上，将 AI 技术与物流核心业务进行深度融合。

**技术栈：Java + Python + LangChain + LangGraph + XGBoost + BERT + Spring Cloud Alibaba**

### 核心特性
- 🏗️ **真·微服务架构**：22 个独立微服务，独立部署，边界清晰
- 🤖 **AI 深度融合**：11 个 AI 业务场景，覆盖风控、调度、识别、计算等核心环节
- 📱 **四端完整**：用户端、快递员端、司机端、管理端全业务覆盖
- 🔒 **生产级安全**：JWT 双 Token 三验证、分布式锁、分布式事务
- 🚀 **智能调度系统**：运输任务调度、取件任务调度、派件任务调度

---

## 二、AI 技术亮点

### 2.1 技术架构
**整体技术路线：** `Java（业务系统）→ Python（AI 服务）`

`Python` · `LangChain` · `LangGraph` · `XGBoost` · `BERT` · `RabbitMQ` · `MongoDB` · `Redis`

### 2.2 AI 功能模块

| 模块 | 功能说明与技术亮点 |
| :---: | --- |
| **AI 登录风控** | 根据用户登录信息与历史记录智能判断风险，高风险触发验证码，低风险直接放行 |
| **Token 刷新风控** | 在双 Token 三验证架构中引入 AI 风控，防止 Token 滥用 |
| **运费计算助手** | 智能体根据自然语言提取城市、重量等信息，自动调用工具计算运费，输出结构化 JSON |
| **快递物品识别** | BERT 模型完成物品分类识别与违禁品判断，包含完整训练流程 |
| **组织机构识别** | 智能判断机构类型（OLT/TLT/AGENCY/UNKNOWN），提升数据录入准确性 |
| **路线成本因子分析** | AI 分析路线成本因子，为物流线路规划提供精准决策依据 |
| **智能推荐网点** | LangGraph 多节点流程：模糊地址解析 → 候选网点 → 智能推荐 → 理由生成 |
| **快递员智能调度** | XGBoost 预测最合适快递员，LLM 给出可解释推荐理由 |
| **运单智能合并** | XGBoost 预测合并概率，Redis ZSet 实现优先级队列（AI 80% + 等待时间 20%） |
| **智能调度验证** | XGBoost 验证任务合理性（装载率、订单组合等），不合理自动触发优化 |
| **模型持续训练** | Java 通过 RabbitMQ 发送训练样本，MongoDB 为数据源，实现 XGBoost 持续更新 |

---

## 三、系统架构

### 3.1 微服务架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        客户端 (Web/App/小程序)                    │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                    Spring Cloud Gateway (9527)                   │
│         统一认证鉴权 · 路由转发 · 限流熔断 · Token 过滤           │
└──┬──────────┬──────────┬──────────┬──────────┬──────────┬───────┘
   │          │          │          │          │          │
┌──▼──┐  ┌───▼───┐  ┌──▼───┐  ┌──▼───┐  ┌──▼───┐  ┌──▼──────┐
│Web  │  │ Web   │  │ Web  │  │ Web   │  │ AIGC │  │ 其他业务 │
│Courier│Customer│Driver│Manager│  │Service│  │ 微服务集群 │
│18090 │  │ 18091 │  │ 18092│  │ 18093 │  │ 18200 │  │         │
└──────┘  └───────┘  └──────┘  └───────┘  └───┬───┘  └─────────┘
                                               │
                                    ┌──────────▼──────────┐
                                    │   Python AI 服务     │
                                    │  (chain-cloud-agent-server)   │
                                    │  LangChain/LangGraph │
                                    │  XGBoost/BERT        │
                                    └─────────────────────┘
```

### 3.2 服务清单

| 服务名称 | 模块 | 端口 | 说明 |
|---------|------|------|------|
| **网关** | chain-cloud-gateway | 9527 | Spring Cloud Gateway，统一入口 |
| **认证服务** | chain-cloud-ms-auth-service | 18100 | 登录、登录记录、Token 管理 |
| **基础服务** | chain-cloud-ms-base-service | 18081 | 区域、货物类型、车辆、司机、排班 |
| **订单服务** | chain-cloud-ms-oms-service | 18082 | 订单、货物、热门商品 |
| **用户服务** | chain-cloud-ms-user-service | 18084 | 会员、地址簿 |
| **作业服务** | chain-cloud-ms-work-service | 18085 | 运输任务、运输订单、揽收调度 |
| **调度服务** | chain-cloud-ms-dispatch-service | 18086 | 智能调度核心 |
| **运输信息服务** | chain-cloud-ms-transport-info-service | 18087 | 运输信息查询 |
| **快递员服务** | chain-cloud-ms-courier-service | 18088 | 快递员业务 |
| **服务范围服务** | chain-cloud-ms-service-scope-service | 18089 | 作业范围管理 |
| **快递员端 Web** | chain-cloud-ms-web-courier | 18090 | 快递员端接口 |
| **客户端 Web** | chain-cloud-ms-web-customer | 18091 | 客户/货主端接口 |
| **司机端 Web** | chain-cloud-ms-web-driver | 18092 | 司机端接口 |
| **管理端 Web** | chain-cloud-ms-web-manager | 18093 | 后台管理接口 |
| **运费服务** | chain-cloud-ms-carriage-service | 18094 | 运费计算 |
| **短信服务** | chain-cloud-ms-sms-service | 18095 | 短信发送 |
| **运输服务** | chain-cloud-ms-transport-service | 18096 | 线路、成本配置、网点 |
| **轨迹服务** | chain-cloud-ms-track-service | 18097 | 物流轨迹 |
| **司机服务** | chain-cloud-ms-driver-service | 18098 | 司机业务 |
| **搜索服务** | chain-cloud-ms-search-service | 18099 | 搜索功能 |
| **交易服务** | chain-cloud-ms-trade-service | 18079 | 支付、退款、通知 |
| **AIGC 服务** | chain-cloud-ms-aigc-service | 18200 | AI 智能服务（风控、调度、识别） |

### 3.3 基础模块

| 模块 | 说明 |
|------|------|
| chain-cloud-common | 通用工具、注解、异常、配置、拦截器 |
| chain-cloud-ms-domain | 领域模型（DTO、枚举、常量） |
| chain-cloud-ms-api | Feign 接口定义 |
| chain-cloud-mq | RabbitMQ 封装（重试、失败持久化、XXL-Job 重试） |

---

## 四、技术栈全景

### 4.1 核心技术

| 分类 | 技术组件 |
|------|----------|
| **AI 技术** | Python · LangChain · LangGraph · XGBoost · BERT · NLP · Agent |
| **微服务框架** | Spring Cloud Alibaba · Spring Cloud Gateway · Nacos · Sentinel · Seata |
| **存储与缓存** | Neo4j（图数据库）· MongoDB · Redis · Caffeine（多级缓存）· MySQL 8.0 |
| **消息与调度** | RabbitMQ · XXL-Job · Redisson（分布式锁） |
| **工具与运维** | EagleMap · JWT · Knife4j · Hutool · MyBatis-Plus |

### 4.2 微服务解决方案

- 🔐 **分布式锁**：Redisson 实现
- 🚀 **多级缓存**：Caffeine + Redis 二级缓存
- 🌐 **分布式网关**：Spring Cloud Gateway 统一认证鉴权
- 📨 **分布式消息**：RabbitMQ 消息队列
- 🔄 **幂等性设计**：服务接口幂等控制
- 🛡️ **缓存问题**：穿透、击穿、雪崩解决方案
- ⚡ **熔断降级**：Sentinel 服务保护
- 💰 **分布式事务**：Seata 1.7.1

---

## 五、工程实践亮点

### 5.1 真·微服务项目
- 代码管理采用独立项目管理（非单项目多模块），更接近真实企业场景
- 微服务边界经过合理设计，独立业务独立微服务
- 在已有系统体系下开发与测试，模拟真实企业入职开发场景
- 真实 Bug 修复训练，培养分析与修复问题的能力

### 5.2 业务真实完整
- 完成四端（用户端、快递员端、司机端、管理端）以及主业务流程
- **智能调度系统**：运输任务调度 / 取件任务调度 / 派件任务调度
- 真实模拟车辆运输，实现数据动态化
- 业务细节做到极致，严格按生产级标准设计

### 5.3 核心业务功能
1. Spring Cloud Gateway 统一认证鉴权
2. JWT 双 Token 三验证的设计与实现
3. 基于 Redisson 实现分布式锁
4. 支付微服务的设计与实现
5. 运费微服务的设计实现
6. 基于图数据库 Neo4j 的路线规划设计与实现
7. 物流业务中的智能调度系统
8. 基于 MongoDB 实现作业范围
9. 基于 Caffeine + Redis 的多级缓存
10. Redis 缓存穿透、击穿、雪崩问题分析与解决

---

## 六、快速开始

### 6.1 环境要求
- JDK 11+
- Maven 3.6+
- MySQL 8.0
- Redis
- RabbitMQ
- Nacos
- MongoDB
- Neo4j

### 6.2 构建项目

```bash
# 克隆项目
git clone <repository-url>
cd chain-cloud

# 构建所有模块
mvn clean install -DskipTests

# 编译单个模块
mvn clean compile -pl chain-cloud-common
```

### 6.3 配置说明

项目使用 Nacos 作为配置中心，主要配置项：

| 配置项 | 说明 |
|--------|------|
| Nacos 地址 | `192.168.150.101:8848` |
| Nacos 用户名 | `nacos` |
| Nacos 密码 | `nacos` |
| 命名空间 | `ecae68ba-7b43-4473-a980-4ddeb6157bdc` |
| 共享配置组 | `SHARED_GROUP` |

共享配置文件：
- `shared-spring-mysql.yml` - MySQL 配置
- `shared-spring-mybatis-plus.yml` - MyBatis-Plus 配置
- `shared-spring-rabbitmq.yml` - RabbitMQ 配置
- `shared-spring-redis.yml` - Redis 配置
- `shared-spring-seata.yml` - Seata 配置
- `shared-spring-xxl-job.yml` - XXL-Job 配置
- `shared-spring-authority.yml` - 认证配置
- `shared-spring-eaglemap.yml` - 地图服务配置
- `shared-spring-mongodb.yml` - MongoDB 配置
- `shared-spring-neo4j.yml` - Neo4j 配置

### 6.4 启动顺序

1. 启动基础设施（Nacos、MySQL、Redis、RabbitMQ、MongoDB、Neo4j）
2. 启动网关：`chain-cloud-gateway` (9527)
3. 启动基础服务：`chain-cloud-ms-base-service` (18081)
4. 启动业务服务（按需启动）
5. 启动 Web 接口模块（按需启动）

---

## 七、项目结构

```
chain-cloud/
├── chain-cloud-common/                # 通用模块
├── chain-cloud-ms-domain/             # 领域模型层
├── chain-cloud-ms-api/                # API 层（Feign 接口）
├── chain-cloud-mq/                    # 消息队列模块
├── chain-cloud-gateway/               # API 网关 (9527)
├── chain-cloud-ms-auth-service/       # 认证服务 (18100)
├── chain-cloud-ms-base-service/       # 基础服务 (18081)
├── chain-cloud-ms-oms-service/        # 订单服务 (18082)
├── chain-cloud-ms-user-service/       # 用户服务 (18084)
├── chain-cloud-ms-work-service/       # 作业服务 (18085)
├── chain-cloud-ms-dispatch-service/   # 调度服务 (18086)
├── chain-cloud-ms-transport-info-service/  # 运输信息服务 (18087)
├── chain-cloud-ms-courier-service/    # 快递员服务 (18088)
├── chain-cloud-ms-service-scope-service/  # 服务范围服务 (18089)
├── chain-cloud-ms-trade-service/      # 交易服务 (18079)
├── chain-cloud-ms-carriage-service/   # 运费服务 (18094)
├── chain-cloud-ms-driver-service/     # 司机服务 (18098)
├── chain-cloud-ms-track-service/      # 轨迹服务 (18097)
├── chain-cloud-ms-sms-service/        # 短信服务 (18095)
├── chain-cloud-ms-search-service/     # 搜索服务 (18099)
├── chain-cloud-ms-transport-service/  # 运输服务 (18096)
├── chain-cloud-ms-aigc-service/       # AIGC 智能服务 (18200)
├── chain-cloud-ms-web-courier/        # 快递员端 Web (18090)
├── chain-cloud-ms-web-customer/       # 客户端 Web (18091)
├── chain-cloud-ms-web-driver/         # 司机端 Web (18092)
└── chain-cloud-ms-web-manager/        # 管理端 Web (18093)
```

---

## 八、AI 服务集成

### 8.1 AIGC 模块架构

AIGC 模块 (`chain-cloud-ms-aigc-service`) 作为 Java 业务系统与 Python AI 服务之间的代理/编排层：

```
Java 微服务 ──Feign──▶ AIGC 模块 ──HTTP──▶ Python AI 服务 (chain-cloud-agent-server)
                              │
                              ├── Agent ID 1001: 登录风控
                              ├── Agent ID 1002: Token 刷新风控
                              ├── Agent ID 1003: 运费计算
                              ├── Agent ID 1004: 物品识别
                              ├── Agent ID 1005: 组织机构识别
                              ├── Agent ID 1006: 成本因子分析
                              ├── Agent ID 1007: 服务范围
                              ├── Agent ID 1008: 快递员调度
                              ├── Agent ID 1009: 运输订单调度
                              └── Agent ID 1010: 运输任务调度
```

### 8.2 AI Agent 列表

| Agent | Agent ID | 功能 |
|-------|----------|------|
| 登录风控 | 1001 | 登录风险评估 |
| Token 刷新风控 | 1002 | 刷新 Token 风险评估 |
| 运费计算 | 1003 | 自然语言运费计算 |
| 物品识别 | 1004 | 包裹物品验证 |
| 组织机构识别 | 1005 | 机构类型分类 |
| 成本因子分析 | 1006 | 路线成本因子分析 |
| 服务范围 | 1007 | 服务范围确定 |
| 快递员调度 | 1008 | 快递员智能调度 |
| 运输订单调度 | 1009 | 运输订单优先级评分 |
| 运输任务调度 | 1010 | 运输任务预测 |

---

## 九、就业竞争力

### 能力维度

| 能力维度 | 说明 |
|---------|------|
| **Java 微服务开发** | Spring Cloud Alibaba 全套、分布式解决方案实战 |
| **AI 工程化落地** | LangChain/LangGraph 智能体开发、XGBoost/BERT 模型训练与集成 |
| **Python AI 服务** | Java + Python 双语言联动，AI 服务独立部署 |
| **复杂系统开发** | 生产级项目、Bug 修复、需求分析到实现完整链路 |

### 简历核心亮点

- 参与生产级微服务物流系统开发，熟悉 Spring Cloud Alibaba 技术体系
- 掌握 LangChain / LangGraph 智能体开发，落地多个 AI 业务场景
- 基于 XGBoost 完成快递员调度、运单合并预测等机器学习任务工程化落地
- 实现 BERT 模型训练及部署，完成物品分类识别业务
- 熟悉分布式锁、多级缓存、分布式事务、链路追踪等微服务核心解决方案

---

## 十、学习目标

1. 具备将复杂业务进行分解、逐步实现需求的能力
2. 具备微服务边界划分与架构设计能力
3. 具备绘制系统流程图、时序图的业务分析能力
4. 具备对现有功能进行升级改造的能力
5. 具备使用 AI 技术（LangChain / LangGraph / XGBoost / BERT）落地业务的能力
6. 具备多级缓存（分布式缓存）设计与使用能力
7. 具备封装通用组件 / 中台服务的能力
8. 具备分布式事务解决能力
9. 具备高并发安全与幂等问题处理能力
10. 具备全链路追踪的解决能力
11. 具备解决较高难度 Bug 的能力

---

## 十一、注意事项

- 使用 **Lombok**，需要 IDE 安装 Lombok 插件
- 根 pom 为聚合工程，不打包 jar
- `chain-cloud-common`、`chain-cloud-ms-domain`、`chain-cloud-ms-api`、`chain-cloud-mq` 为纯依赖库模块
- 使用 `@NoAuthorization` 注解可跳过接口鉴权
- 全局异常由 `GlobalExceptionHandler` 统一处理
- Swagger 文档使用 Knife4j (3.0.3)
- 服务注册与配置中心使用 Nacos

---

## 十二、许可证

本项目仅供学习与研究使用。

---

**智链云途** - 让物流更智能 🚚🤖
