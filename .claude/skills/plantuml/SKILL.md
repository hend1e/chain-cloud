---
name: plantuml
description: 定义项目中 PlantUML 图代码生成规范,包括时序图、流程图等图表的编写规范和最佳实践
---

## PlantUML 图代码生成规范

### 1. 基本结构

每个 PlantUML 文件必须遵循以下基本结构:

@startuml

autonumber

actor "参与者名称" as alias
participant "组件名称" as alias

...

@enduml

### 2. 参与者定义规范

#### 2.1 参与者类型与关键字

| 类型 | 关键字 | 用途 | 示例 |
|------|--------|------|------|
| 外部角色 | actor | 用户、前端页面、外部系统 | actor "用户" as user |
| 内部组件 | participant | 微服务、数据库、中间件 | participant "订单微服务" as oms |
| 边界组件 | boundary | API 网关、边界服务 | boundary "网关" as gateway |
| 控制组件 | control | 调度器、控制器 | control "调度中心" as dispatch |
| 数据库 | database | 数据库、存储 | database "MySQL" as mysql |
| 集合 | collections | 缓存、消息队列 | collections "Redis" as redis |

#### 2.2 命名规范

- 显示名称:使用中文,清晰描述业务含义,用双引号包裹
- 别名:使用英文小写,简洁明了,用 as 关键字定义
- 微服务命名:使用项目中的模块简称,如 manager、customer、oms、work 等
- 中间件命名:使用技术名称,如 redis、mysql、mq、rabbitmq 等

### 3. 消息交互规范

#### 3.1 消息箭头类型

| 箭头语法 | 含义 | 用途 |
|----------|------|------|
| -> | 实线箭头 | 同步调用 |
| --> | 虚线箭头 | 返回响应 |
| ->> | 实线开放箭头 | 异步消息 |
| -->> | 虚线开放箭头 | 异步返回 |
| -[#red]-> | 红色实线箭头 | 异常/失败流程 |
| -[#green]-> | 绿色实线箭头 | 成功流程 |

#### 3.2 激活/停用规范

- 使用 ++ 激活参与者(开始处理)
- 使用 -- 停用参与者(处理结束)
- 使用 deactivate 显式停用
- 使用 activate 显式激活

#### 3.3 消息文本规范

- 使用冒号后跟消息描述
- 描述应简洁明了,说明操作内容
- 长文本使用 \n 换行
- 中文描述,清晰表达业务含义

### 4. 分组规范

#### 4.1 group 语法

使用 group 关键字对相关业务步骤进行分组:

group 业务阶段名称
  业务步骤
end

#### 4.2 分组命名规范

- 使用中文命名
- 名称应概括该组业务的核心功能
- 常见分组:下单业务、快递员取派件任务调度、快递员取派件任务存储、登录验证、支付流程 等

### 5. 自动编号

- 必须 在文件开头添加 autonumber 指令
- 自动为每个消息交互添加序号
- 便于文档引用和讨论

### 6. 注释规范

- 使用 ' 添加单行注释
- 注释用于说明设计意图或特殊处理
- 注释不会渲染在输出的图中

### 7. 完整示例

#### 7.1 登录流程示例

@startuml

autonumber

actor "前端页面" as web
participant "chain-cloud-ms-web-manager" as manager
participant "Redis" as redis

web -> manager --++: 获取验证码(携带key)
manager -> manager: 生成验证码
manager -> redis ++: 存储到Redis,key前端生成,\nvalue是验证码的值
redis --> manager --: 返回
manager --> web --: 响应验证码图片

web -> manager --++: 用户名、密码、验证码
manager -> redis ++: 查询验证码值
redis --> manager --: 返回
manager -> manager: 校验验证码
manager -[#red]-> web: 验证码错误
manager -> redis ++: 删除验证码值
redis --> manager --: 返回
manager -> manager: 校验用户名、密码(基于权限管家)
manager -[#red]-> web: 用户名或密码错误
manager -> manager: 生成token
manager -[#green]-> web --: 登录成功,返回token等信息

@enduml

#### 7.2 智能分配快递员流程示例

@startuml

autonumber

actor "用户" as user
participant "web-customer" as customer
participant "订单微服务" as oms
participant "RabbitMQ" as mq
participant "调度中心" as dispatch
participant "work微服务" as work
participant "MySQL" as mysql

group 下单业务
  user -> customer ++: 下单
  customer -> oms ++: 下单
  oms -> oms: 业务处理
  oms --> mq ++: 发送新订单消息
  return 返回
  return 返回
  return 返回
end

group 快递员取派件任务调度
  mq --> dispatch ++: 接收新订单消息
  dispatch -> dispatch: 查询范围内以及\n有排班的快递员
  dispatch -> dispatch: 计算快递员负载,\n选取快递员
  dispatch -> dispatch: 封装取件/派件任务\n消息对象
  dispatch --> mq: 取件时间小于2小时\n实时消息
  dispatch --> mq: 取件时间大于2小时\n延时消息
  deactivate dispatch
end

group 快递员取派件任务存储
  mq --> work ++: 接收取派件任务消息
  work -> work: 幂等性处理
  work -> mysql ++: 持久化数据
  return 返回
  deactivate work
end

@enduml

### 8. 常见场景模板

#### 8.1 CRUD 操作流程

@startuml

autonumber

actor "前端" as web
participant "API层" as api
participant "业务服务" as service
database "数据库" as db

group 查询流程
  web -> api ++: 查询请求
  api -> service ++: 调用服务
  service -> db ++: 查询数据
  db --> service --: 返回数据
  service --> api --: 返回结果
  api --> web --: 响应数据
end

group 创建流程
  web -> api ++: 创建请求
  api -> service ++: 调用服务
  service -> service: 参数校验
  service -> db ++: 保存数据
  db --> service --: 返回
  service --> api --: 返回结果
  api --> web --: 响应成功
end

@enduml

#### 8.2 异步消息处理流程

@startuml

autonumber

participant "生产者" as producer
participant "消息队列" as mq
participant "消费者" as consumer
database "数据库" as db

producer --> mq ++: 发送消息
mq --> producer --: 确认接收

mq --> consumer ++: 推送消息
consumer -> consumer: 消息处理
consumer -> db ++: 持久化数据
db --> consumer --: 返回
consumer --> mq --: 确认消费
deactivate consumer

@enduml

### 9. 注意事项

- 必须 以 @startuml 开始,以 @enduml 结束
- 必须 使用 autonumber 自动编号
- 推荐 使用 group 对业务流程进行分组
- 推荐 使用颜色标记成功/失败分支([#green]、[#red])
- 避免 过于复杂的嵌套和过多的参与者(建议不超过 8 个)
- 保持 消息描述简洁,长文本使用 \n 换行
- 统一 使用中文描述业务含义,别名使用英文
- 注意 激活/停用的配对使用,避免激活泄漏
- 微服务名称 应与项目中实际模块名称保持一致

### 10. 高级特性

#### 10.1 条件分支

alt 条件成立
  A -> B: 执行操作A
else 条件不成立
  A -> B: 执行操作B
end

#### 10.2 循环

loop 循环条件
  A -> B: 重复操作
end

#### 10.3 可选流程

opt 可选条件
  A -> B: 可选操作
end

#### 10.4 并行处理

par 并行执行
  A -> B: 操作1
parallel
  A -> C: 操作2
end

### 11. 文件命名与存放

- PlantUML 文件以 .puml 为扩展名
- 建议存放在项目文档目录的 diagrams/ 文件夹下
- 文件名应清晰描述图的内容,如 login-flow.puml、order-dispatch-flow.puml
