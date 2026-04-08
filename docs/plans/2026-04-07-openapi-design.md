# OpenAPI Interface Documentation Design

**目标**

基于 OpenAPI 3.x 与 `springdoc-openapi`，为 MealMate Service 建立一套轻量但完整的接口说明体系，确保 AI 与开发者可以通过标准 `/v3/api-docs` 准确获取当前 HTTP 接口的路径、参数、请求体、响应体与核心业务语义。

**范围**

- 仅覆盖当前 Web HTTP 接口文档能力建设
- 基于 `springdoc-openapi` 生成 OpenAPI 3.x 文档
- 覆盖启动层装配、控制器说明、DTO 字段说明与基础访问路径
- 目标是“正确、稳定、可被 AI 消费”，不追求复杂治理能力

---

## 1. 设计结论

### 1.1 总体策略

采用“标准生成 + 最小增强注解”的方案：

- 在 `mealmate-start` 中接入 `springdoc-openapi-starter-webmvc-ui`
- 在 `start` 层新增 OpenAPI 配置类，集中声明文档基础信息
- 在 `adapter.web` 下的 Controller 和 DTO 上补充少量 OpenAPI 注解
- 保持 `domain`、`app`、`infrastructure` 不感知文档框架

该方案满足当前最核心目标：让 AI 能通过标准 OpenAPI 文档稳定理解接口，而不是依赖零散源码推断。

### 1.2 方案选择原因

本次不采用“纯自动扫描”方案，也不采用“复杂文档治理”方案。

不选纯自动扫描的原因：

- 默认生成的字段语义偏弱
- 路径参数与请求体的业务含义不够清楚
- 枚举值、示例值与对象用途说明不足，AI 消费效果不稳定

不选复杂治理方案的原因：

- 当前仓库接口数量很少，仅有 `family` 一组 Web 接口
- 现阶段目标是先把正确的接口信息暴露出来，而不是建设完整 API 门户
- 引入复杂分组、鉴权体系、统一错误模型治理会增加无效成本

### 1.3 分层落点

- `start`：负责 OpenAPI 相关依赖装配、全局文档配置
- `adapter`：负责接口级语义说明与请求/响应模型说明
- `app/domain/infrastructure`：不直接承载 OpenAPI 相关实现

这样可以保持 COLA 约束不被破坏：技术装配在 `start`，协议语义在 `adapter`。

---

## 2. 文档体系设计

### 2.1 输出目标

最终应提供以下标准入口：

- `/v3/api-docs`：机器可读的 OpenAPI JSON
- `/swagger-ui/index.html`：本地校验与人工查看入口

其中，`/v3/api-docs` 是本次的主要交付对象，Swagger UI 仅作为校验辅助，不作为主要设计目标。

### 2.2 OpenAPI 元信息

在全局 `OpenAPI` Bean 中提供以下信息：

- title：`MealMate Service API`
- description：说明服务定位，强调家庭饮食规划场景
- version：使用当前服务版本语义，优先从配置或常量读取

如实现成本较低，可增加：

- contact：项目组织信息
- license：仓库许可信息

但这两项不是本次必需能力。

### 2.3 Tag 设计

当前仅有 `family` 领域的 Web 接口，因此采用最小 Tag 体系：

- `Family`：家庭画像与成员管理相关接口

后续新增 `recipe`、`meal plan`、`record` 等接口时，再按聚合领域扩展 Tag。

### 2.4 路径与操作说明

每个 Controller 方法补齐如下信息：

- `@Operation(summary = ..., description = ...)`
- `@Parameter` 或通过 `@Schema` 间接说明路径参数语义
- 关键返回对象说明

当前 `FamilyMemberController` 中至少应覆盖以下接口说明：

- 查询家庭画像
- 查询家庭成员列表
- 查询单个成员详情
- 新增成员
- 更新成员
- 更新成员偏好
- 删除成员

说明粒度以“AI 可以直接理解用途”为目标，例如：

- `familyId`：家庭唯一标识
- `memberId`：家庭成员唯一标识
- `UpdateMemberPreferenceRequest`：成员口味偏好、忌口与营养目标更新请求

---

## 3. DTO 与枚举说明设计

### 3.1 Request/Response DTO

`adapter.web.family.dto` 下的请求与响应对象应使用 `@Schema` 补充字段说明。

重点字段需要包含：

- 含义
- 示例值
- 是否必填
- 业务备注（如适用）

典型字段示例：

- `name`：成员姓名
- `roleType`：家庭角色类型，例如本人、配偶、宝宝
- `targetType`：饮食目标类型
- `birthday`：出生日期
- `tasteTags`：口味偏好标签列表
- `avoidIngredients`：忌口食材列表
- `allergyIngredients`：过敏食材列表

### 3.2 枚举说明

当前 `adapter.web.family.dto.enums` 下已有多组枚举，这些枚举直接影响 AI 对字段取值的理解，因此需要补全 `@Schema` 说明：

- 枚举整体含义
- 每个取值的业务语义

如果当前项目中的序列化形式就是枚举名，则应确保生成文档中能直接暴露枚举可选值。

### 3.3 COLA 响应包装兼容性

当前控制器返回值是：

- `SingleResponse<T>`
- `MultiResponse<T>`
- `Response`

本次设计不重构这套响应结构，而是优先验证 `springdoc-openapi` 对 COLA DTO 的生成结果是否足够可读。

若默认生成结果存在以下问题，再做最小补救：

- 包装层字段缺失或命名不清晰
- `data` 字段泛型展开不完整
- `MultiResponse` 无法正确表达列表元素类型

补救策略优先级：

1. 优先通过注解改善展示
2. 如有必要，再增加少量 `@Schema` 或 `@ArraySchema`
3. 本次不引入自定义复杂 `ModelConverter`

---

## 4. 配置设计

### 4.1 依赖接入

依赖仅加在 `mealmate-start`，因为：

- `start` 是 Spring Boot 启动与技术装配层
- `adapter` 不应直接承担框架基础设施依赖管理

建议使用：

- `org.springdoc:springdoc-openapi-starter-webmvc-ui`

### 4.2 配置项策略

本次配置保持最小化：

- 默认启用 API docs
- 默认启用 Swagger UI
- 默认扫描当前 Spring MVC Web 接口

如需显式配置，可在 `application.yml` 中补充：

- API docs 路径
- Swagger UI 路径

但不建议在当前阶段增加复杂 profile 开关，因为当前目标是先稳定暴露文档。

### 4.3 包扫描边界

OpenAPI 的接口说明来源应聚焦在 `adapter.web` 控制器，避免未来把非 Web 边界误暴露为 HTTP 文档。

如果采用 `GroupedOpenApi`，建议：

- group 名称：`web`
- paths：`/api/**`
- packages：`io.yggdrasil.labs.mealmate.adapter.web`

当前接口规模很小，即使不分组也可以工作；但增加一层最小分组有助于后续扩展。

---

## 5. 错误处理与限制

### 5.1 当前阶段不做的内容

本次不纳入以下能力：

- 复杂的多分组文档门户
- 基于环境的精细化文档开关
- 统一错误码表导出
- OAuth2 / JWT 安全方案接入
- 静态 OpenAPI 文件导出与发布

### 5.2 风险点

主要风险有两个：

1. `springdoc-openapi` 对 COLA 泛型响应包装的展示可能不够理想
2. 现有 DTO 若缺乏注解说明，生成文档的语义仍然偏弱

应对策略：

- 先最小接入并查看生成结果
- 仅在发现明显问题时增加少量补充注解
- 不为少量展示问题提前引入重型定制

---

## 6. 测试与验收设计

### 6.1 最小验证

至少完成以下验证：

- `mealmate-start` 模块编译通过
- Spring Boot 上下文可以加载 OpenAPI 配置
- `/v3/api-docs` 能被测试或运行时成功访问

### 6.2 建议验证方式

优先新增一个 `mealmate-start` 层的集成测试，校验：

- 上下文启动成功
- 请求 `/v3/api-docs`
- 返回状态码为 `200`
- 返回内容中至少包含：
  - `/api/families/{familyId}`
  - `/api/families/{familyId}/members`
  - `Family`

如果仓库已有 Web 集成测试基建，则优先沿用已有测试方式。

### 6.3 完成标准

当以下条件同时满足时，本次任务视为完成：

- 已接入 `springdoc-openapi`
- 可通过 `/v3/api-docs` 输出合法 OpenAPI 文档
- `family` 相关接口在文档中可见
- 主要 DTO 与枚举已具备基础语义说明
- 至少有一条自动化验证覆盖 OpenAPI 文档入口

---

## 7. 实施建议

建议按照以下顺序实施：

1. 在 `mealmate-start` 接入依赖并补全 OpenAPI 配置
2. 为当前 `FamilyMemberController` 增加 Tag 与 Operation 说明
3. 为 `adapter.web.family.dto` 与枚举补充 `@Schema`
4. 新增集成测试验证 `/v3/api-docs`
5. 视验证结果决定是否补最小响应模型增强

该顺序可以先确保“文档通路打通”，再提升语义质量，避免一开始就陷入细枝末节。
