/**
 * 数据传输对象（DTO）
 *
 * <p>本包属于 Client 层，定义应用层接口的输入输出数据结构。
 *
 * <p><b>职责：</b>
 *
 * <ul>
 *   <li>定义 Command 对象（写操作的输入）
 *   <li>定义 Query 对象（读操作的输入）
 *   <li>定义 CO 对象（读操作的输出）
 *   <li>定义错误码和枚举类型
 * </ul>
 *
 * <p><b>包结构：</b>
 *
 * <ul>
 *   <li><b>cmd</b>：命令对象（Command），用于写操作
 *   <li><b>query</b>：查询对象（Query），用于读操作
 *   <li><b>co</b>：客户对象（Client Object），用于读操作的输出
 *   <li><b>enums</b>：枚举类型，包括错误码等
 * </ul>
 *
 * <p><b>设计原则：</b>
 *
 * <ul>
 *   <li>DTO 应该是纯数据结构，不包含业务逻辑
 *   <li>使用 Lombok 简化代码（@Data、@EqualsAndHashCode）
 *   <li>使用 JSR 303 注解进行参数校验
 *   <li>继承 COLA 框架的基类（Command、Query、DTO）
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>DTO 对象在不同层之间传递，应保持简单和稳定
 *   <li>避免在 DTO 中包含复杂的业务对象，优先使用基本类型
 *   <li>字段命名应清晰表达业务含义
 *   <li>遵循 COLA 5.0 的命名规范
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.mealmate.client.dto;
