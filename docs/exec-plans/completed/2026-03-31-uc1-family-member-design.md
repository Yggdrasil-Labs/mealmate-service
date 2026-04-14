# UC1 Family Member Design

**目标**

为 UC1「维护家庭画像」提供纯后端落地方案。该方案采用成员中心建模：`family` 负责归组与默认配置，`member` 是主要业务主体，后续周计划、记录、营养分析等能力都围绕成员集合与成员偏好展开。

**范围**

- 仅覆盖后端设计
- 覆盖数据库、COLA 分层、接口定义、业务规则、错误码、测试策略与任务拆分
- 当前阶段不考虑 Redis 缓存

---

## 1. 设计结论

### 1.1 建模策略

采用成员中心方案：

- `FamilyProfile` 表达家庭归组、默认地域、整体饮食目标等轻量元数据
- `FamilyMember` 是 UC1 的核心主体对象
- `MemberPreference` 是成员的一对一偏好扩展对象

家庭不是重聚合中心，而是成员的归属容器与默认配置载体。UC1 的主要用例围绕成员的增删改查和偏好维护展开。

### 1.2 分层原则

- `adapter` 只处理 HTTP 入参与响应
- `app` 负责用例编排、事务边界、查询组装
- `domain` 负责成员与偏好相关业务规则
- `infrastructure` 负责 DO、Repository 实现、对象转换

### 1.3 实现偏好

- 优先使用 Lombok 减少 DTO、CO、Entity、DO 的样板代码
- 优先使用 MapStruct 完成对象映射
- 列表字段、JSON 字段、枚举字段的特殊转换通过 MapStruct 自定义方法补足
- 数据库结构变更统一通过 Flyway 管理，脚本放在 Spring Boot 默认扫描路径

---

## 2. 数据库设计

### 2.1 family_profile

用途：家庭归组与默认配置

保留字段：

- `id`
- `family_name`
- `family_code`
- `status`
- `region`
- `meal_goal_json`
- `remark`
- `created_at`
- `updated_at`
- `created_by`
- `updated_by`
- `deleted`

约束建议：

- `family_code` 唯一
- `(status, deleted)` 普通索引

删除策略：

- 逻辑删除，保留 `deleted` 字段

### 2.2 family_member

用途：UC1 核心主表，表达成员基本业务信息

保留字段：

- `id`
- `family_id`
- `name`
- `role_type`
- `gender`
- `birthday`
- `region`
- `target_type`
- `avatar_url`
- `sort_no`
- `status`
- `created_at`
- `updated_at`
- `created_by`
- `updated_by`
- `deleted`

设计说明：

- 不建议长期存储 `age`，统一通过 `birthday` 在查询侧推导
- `GUEST` 角色允许存在，但默认不作为后续计划生成核心输入
- 成员删除采用逻辑删除

索引建议：

- `(family_id, deleted)`
- `(family_id, status, deleted)`
- `(family_id, role_type, deleted)`

### 2.3 member_preference

用途：成员偏好与限制，一对一挂载在成员上

保留字段：

- `id`
- `member_id`
- `taste_tags`
- `avoid_ingredients`
- `allergy_ingredients`
- `spicy_level`
- `sweet_level`
- `oil_level`
- `salt_level`
- `nutrition_goal_json`
- `extra_rule_json`
- `created_at`
- `updated_at`
- `created_by`
- `updated_by`

设计说明：

- `member_id` 唯一
- `taste_tags`、`avoid_ingredients`、`allergy_ingredients` 当前阶段可先存 `VARCHAR`
- 领域和应用层统一以 `List<String>` 使用
- `nutrition_goal_json`、`extra_rule_json` 保持 JSON，便于后续扩展

删除策略：

- 物理删除，跟随成员生命周期

### 2.4 Flyway 变更约定

- 所有数据库结构变更通过 Flyway 版本脚本提交
- 脚本路径使用 `mealmate-start/src/main/resources/db/migration/`
- 脚本命名遵循 `V{版本}__{描述}.sql`
- 新增脚本前必须先检查仓库现有 migration 的最大版本号，并使用下一个可用版本
- 脚本提交后不修改，只允许新增后续版本脚本
- 新增字段必须有默认值或允许 `NULL`

---

## 3. 领域模型设计

### 3.1 领域对象

- `FamilyProfile`
- `FamilyMember`
- `MemberPreference`

### 3.2 建议枚举

- `FamilyStatus`
- `MemberStatus`
- `MemberRoleType`
- `GenderType`
- `MemberTargetType`
- `SpicyLevel`
- `SweetLevel`
- `OilLevel`
- `SaltLevel`

### 3.3 领域服务

`FamilyDomainService` 负责以下规则：

- 新增成员前校验家庭存在且有效
- 更新或删除成员前校验成员存在且归属当前家庭
- `BABY` 角色偏好限制：
  - 不允许 `spicyLevel` 为 `MILD/MEDIUM/HOT`
  - 不允许 `saltLevel` 为 `SALTY`
  - 不允许 `oilLevel` 为 `RICH`
- 偏好列表字段统一做去空、去重、去首尾空白

说明：

- 不限制一个家庭中 `BABY` 的数量

---

## 4. COLA 分层落点

### 4.1 Adapter

- `adapter.web.family.FamilyMemberController`
- `adapter.web.family.dto.AddFamilyMemberRequest`
- `adapter.web.family.dto.UpdateFamilyMemberRequest`
- `adapter.web.family.dto.UpdateMemberPreferenceRequest`
- `adapter.web.family.dto.FamilyProfileResponse`
- `adapter.web.family.dto.FamilyMemberResponse`
- `adapter.web.family.dto.MemberPreferenceResponse`
- `adapter.web.family.convertor.FamilyMemberWebConvertor`

### 4.2 App

- `app.family.application.FamilyMemberAppService`
- `app.family.dto.cmd.AddFamilyMemberCmd`
- `app.family.dto.cmd.UpdateFamilyMemberCmd`
- `app.family.dto.cmd.UpdateMemberPreferenceCmd`
- `app.family.dto.cmd.RemoveFamilyMemberCmd`
- `app.family.dto.qry.GetFamilyProfileQry`
- `app.family.dto.qry.GetFamilyMemberListQry`
- `app.family.dto.qry.GetFamilyMemberDetailQry`
- `app.family.dto.co.FamilyProfileCO`
- `app.family.dto.co.FamilyMemberCO`
- `app.family.dto.co.MemberPreferenceCO`
- `app.family.executor.AddFamilyMemberCmdExe`
- `app.family.executor.UpdateFamilyMemberCmdExe`
- `app.family.executor.UpdateMemberPreferenceCmdExe`
- `app.family.executor.RemoveFamilyMemberCmdExe`
- `app.family.executor.GetFamilyProfileQryExe`
- `app.family.executor.GetFamilyMemberListQryExe`
- `app.family.executor.GetFamilyMemberDetailQryExe`
- `app.family.assembler.FamilyMemberAssembler`
- `app.family.convertor.FamilyMemberConvertor`

### 4.3 Domain

- `domain.family.model.FamilyProfile`
- `domain.family.model.FamilyMember`
- `domain.family.model.MemberPreference`
- `domain.family.repo.FamilyProfileRepository`
- `domain.family.repo.FamilyMemberRepository`
- `domain.family.repo.MemberPreferenceRepository`
- `domain.family.service.FamilyDomainService`

### 4.4 Infrastructure

- `infrastructure.persistence.family.dataobject.FamilyProfileDO`
- `infrastructure.persistence.family.dataobject.FamilyMemberDO`
- `infrastructure.persistence.family.dataobject.MemberPreferenceDO`
- `infrastructure.persistence.family.convertor.FamilyProfileInfraConvertor`
- `infrastructure.persistence.family.convertor.FamilyMemberInfraConvertor`
- `infrastructure.persistence.family.convertor.MemberPreferenceInfraConvertor`
- `infrastructure.persistence.family.mapper.FamilyProfileMapper`
- `infrastructure.persistence.family.mapper.FamilyMemberMapper`
- `infrastructure.persistence.family.mapper.MemberPreferenceMapper`
- `infrastructure.persistence.impl.FamilyProfileRepositoryImpl`
- `infrastructure.persistence.impl.FamilyMemberRepositoryImpl`
- `infrastructure.persistence.impl.MemberPreferenceRepositoryImpl`

说明：

- `dataobject` 沿用仓库现有约定，避免使用 Java 关键字 `do` 作为包段
- Mapper 优先遵循 `@AutoMybatis` 生成约定，避免重复手写等价样板
- `RepositoryImpl` 放回 `persistence.impl`，与仓库既有 COLA 约定保持一致

---

## 5. 接口设计

### 5.1 查询家庭基础信息

- `GET /api/families/{familyId}`

返回：

- `familyId`
- `familyName`
- `region`
- `mealGoal`

### 5.2 查询家庭成员列表

- `GET /api/families/{familyId}/members`

返回：

- 成员基础信息
- 偏好摘要

### 5.3 查询成员详情

- `GET /api/families/{familyId}/members/{memberId}`

返回：

- 成员基本信息
- 完整偏好信息

### 5.4 新增成员

- `POST /api/families/{familyId}/members`

请求字段建议：

- `name`
- `roleType`
- `gender`
- `birthday`
- `region`
- `targetType`
- `avatarUrl`
- `sortNo`

### 5.5 更新成员基本信息

- `PUT /api/families/{familyId}/members/{memberId}`

### 5.6 更新成员偏好

- `PUT /api/families/{familyId}/members/{memberId}/preference`

请求字段建议：

- `tasteTags`
- `avoidIngredients`
- `allergyIngredients`
- `spicyLevel`
- `sweetLevel`
- `oilLevel`
- `saltLevel`
- `nutritionGoal`
- `extraRule`

### 5.7 删除成员

- `DELETE /api/families/{familyId}/members/{memberId}`

语义：

- 成员逻辑删除
- 同一事务内物理删除该成员对应的 `member_preference`

### 5.8 返回结构

- Controller 层统一返回 COLA 标准结构
- 单对象使用 `SingleResponse<T>`
- 列表使用 `MultiResponse<T>`
- 分页使用 `PageResponse<T>`
- Controller 不直接返回裸对象

---

## 6. 对象转换约定

### 6.1 Adapter 转换

- Request DTO -> Cmd/Qry

### 6.2 App 转换

- Cmd -> Domain
- Domain -> CO

### 6.3 Infra 转换

- Entity <-> DO

### 6.4 特殊字段处理

- `tasteTags`、`avoidIngredients`、`allergyIngredients`
  - 上层使用 `List<String>`
  - 存储层使用逗号分隔字符串
- `nutritionGoalJson`、`extraRuleJson`
  - 上层使用 `Map<String, Object>`
  - 存储层使用 JSON
- 枚举字段统一在 MapStruct 转换器中处理

---

## 7. 错误码建议

- `FAMILY_NOT_FOUND`
- `FAMILY_MEMBER_NOT_FOUND`
- `PREFERENCE_INVALID_FOR_BABY`
- `MEMBER_ROLE_INVALID`
- `MEMBER_STATUS_INVALID`
- `MEMBER_TARGET_TYPE_INVALID`
- `MEMBER_PREFERENCE_INVALID`

说明：

- 业务异常统一继承 `BizException`
- errCode 风格遵循 `{DOMAIN}_{TYPE}_{BRIEF}`，当前 UC1 以 `FAMILY_*` 为前缀

---

## 8. 测试策略

### 8.1 Domain 单测

- `BABY` 成员禁止成人化偏好
- 偏好列表字段标准化
- 成员归属校验规则

### 8.2 App 集成测试

- 新增成员
- 更新成员基本信息
- 更新成员偏好
- 删除成员，并校验 `member_preference` 被同步物理删除
- 查询成员详情
- 查询家庭成员列表

### 8.3 Infrastructure 测试

- DO 与 Entity 的 MapStruct 映射
- 列表字段序列化与反序列化
- JSON 字段转换

### 8.4 Adapter 测试

- Controller 返回结构符合 `SingleResponse` / `MultiResponse`
- `@Validated` 生效
- 异常通过全局处理器转换，不在 Controller 层做 `try-catch`

### 8.5 Flyway 验证

- 校验 `mealmate-start` 能扫描到新增 migration
- 至少执行一次空库迁移 smoke test
- 校验迁移后关键表可被应用正常访问

---

## 9. 任务拆分

1. 编写三张表的 DDL 与迁移脚本
2. 定义 family 领域模型、枚举、仓储接口和领域服务
3. 实现 DO、MapStruct InfraConvertor、RepositoryImpl
4. 实现 Cmd/Qry、CO、Executor、AppService
5. 实现 Controller、Request DTO、WebConvertor
6. 补齐领域测试、应用测试和编译验证

---

## 10. 实施约束

- 优先使用 Lombok
- 优先使用 MapStruct
- 严格遵守 COLA 依赖方向
- 当前阶段不引入 Redis 缓存
- Flyway 脚本以 `mealmate-start` 资源目录为准，不再额外维护独立 schema 文件作为执行入口
- 默认不执行提交、推送、发版动作，除非用户明确要求
