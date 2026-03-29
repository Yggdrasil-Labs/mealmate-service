/**
 * 领域层（Domain 层）
 *
 * <p>本包属于 DDD 架构中的 Domain 层（领域层），包含核心业务逻辑和领域模型。
 *
 * <p><b>职责：</b>
 *
 * <ul>
 *   <li>定义领域模型（Entity、Value Object）
 *   <li>实现领域服务（Domain Service）
 *   <li>定义仓储接口（Repository Interface）
 * </ul>
 *
 * <p><b>骨架规范</b>：新项目应按领域在 <code>io.yggdrasil.labs.mealmate.domain.{domain}</code> 下组织代码，典型子包为
 * <code>
 * model</code>、<code>service</code>、<code>repo</code>。
 *
 * <p>MealMate 业务领域代码请在本包下按聚合根与领域边界继续扩展。
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.mealmate.domain;
