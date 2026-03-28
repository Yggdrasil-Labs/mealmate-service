/**
 * 枚举类型定义
 *
 * <p>本包属于 Client 层的 DTO 模块，定义应用层使用的枚举类型。
 *
 * <p><b>职责：</b>
 *
 * <ul>
 *   <li>定义错误码枚举（ErrorCode）
 *   <li>定义业务状态枚举
 *   <li>定义通用枚举类型
 * </ul>
 *
 * <p><b>类命名规则：</b>
 *
 * <ul>
 *   <li>错误码：ErrorCode
 *   <li>业务枚举：{Domain}{Type}Enum，示例：CustomerStatusEnum、OrderTypeEnum
 *   <li>通用枚举：{Type}Enum，示例：GenderEnum、YesNoEnum
 * </ul>
 *
 * <p><b>错误码规范：</b>
 *
 * <ul>
 *   <li>错误码格式：B_{Domain}_{ErrorType}
 *   <li>示例：B_CUSTOMER_companyNameConflict（客户公司名冲突）
 *   <li>错误码应包含 errCode（错误码）和 errDesc（错误描述）
 * </ul>
 *
 * <p><b>使用场景：</b>
 *
 * <ul>
 *   <li>在 Response 中返回错误码
 *   <li>在业务逻辑中抛出业务异常
 *   <li>在 DTO 中定义枚举字段
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>枚举类型应该是稳定的，变更需要谨慎
 *   <li>错误码应该清晰表达错误原因
 *   <li>业务枚举应与 Domain 层的枚举保持一致（或通过转换）
 *   <li>使用 Lombok 的 @Getter 简化代码
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.midgard.client.dto.enums;
