/**
 * 客户端层（Client Layer）
 *
 * <p>本包是 COLA 5.0 架构的 Client 层，定义对外契约层，不包含任何实现。
 *
 * <p><b>核心职责：</b>
 *
 * <ul>
 *   <li>定义对外提供的业务接口（Client API）
 *   <li>定义数据传输对象（DTO）：Command、Query、CO
 *   <li>定义枚举类型和错误码
 *   <li>作为系统的对外契约，供外部集成方或 RPC/Feign 调用方使用
 * </ul>
 *
 * <p><b>包结构：</b>
 *
 * <ul>
 *   <li><b>api</b>：业务接口定义（Client API），按聚合根组织
 *   <li><b>dto</b>：数据传输对象
 *       <ul>
 *         <li><b>cmd</b>：命令对象（Command），写操作的输入
 *         <li><b>qry</b>：查询对象（Qry），读操作的输入
 *         <li><b>co</b>：客户对象（Client Object），读操作的输出
 *         <li><b>enums</b>：枚举类型和错误码
 *       </ul>
 * </ul>
 *
 * <p><b>设计原则：</b>
 *
 * <ul>
 *   <li><b>契约稳定</b>：Client 层是对外契约，变更需谨慎，影响所有调用方
 *   <li><b>只定义不实现</b>：只包含接口和 DTO 定义，不包含任何实现代码
 *   <li><b>参数校验</b>：在 Command 和 Query 上使用 JSR 303 注解进行参数校验
 *   <li><b>业务语义</b>：接口和 DTO 命名应清晰表达业务意图
 *   <li><b>向后兼容</b>：新增字段时考虑向后兼容性
 * </ul>
 *
 * <p><b>命名规范：</b>
 *
 * <ul>
 *   <li>Client 接口：{Domain}Client（如 CustomerClient）
 *   <li>Command：{Verb}{Domain}Cmd（如 CreateCustomerCmd）
 *   <li>Query：{Verb}{Domain}Qry（如 ListCustomerQry）
 *   <li>Client Object：{Domain}CO（如 CustomerCO）
 * </ul>
 *
 * <p><b>依赖关系：</b>
 *
 * <ul>
 *   <li><b>被依赖</b>：外部调用方或需要共享契约的集成模块依赖 Client 层
 *   <li><b>依赖</b>：只依赖 COLA 框架的基础类（Command、Query、DTO、Response）
 *   <li><b>不依赖</b>：不依赖其他任何层（Start、Adapter、App、Domain、Infrastructure）
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>Client 层是对外契约，修改需要与调用方协商
 *   <li>DTO 对象应保持简单，使用基本类型或包装类型
 *   <li>避免在 DTO 中包含复杂的业务对象或技术细节
 *   <li>使用 Lombok 简化代码（@Data、@EqualsAndHashCode）
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.midgard.client;
