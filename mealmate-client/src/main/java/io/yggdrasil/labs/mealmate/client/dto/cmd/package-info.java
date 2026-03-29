/**
 * 命令对象（Command）
 *
 * <p>本包属于 Client 层的 DTO 模块，定义写操作（Command）的输入参数。
 *
 * <p><b>职责：</b>
 *
 * <ul>
 *   <li>定义写操作（创建、更新、删除等）的输入参数
 *   <li>封装业务操作的请求数据
 *   <li>作为 Facade 写操作方法的参数
 * </ul>
 *
 * <p><b>类命名规则：</b>
 *
 * <p>遵循 COLA 5.0 命名规范，使用固定动词映射表：
 *
 * <ul>
 *   <li><b>Create</b>：创建新资源，示例：CreateCustomerCmd、CreateOrderCmd
 *   <li><b>Update</b>：更新整个资源，示例：UpdateCustomerCmd、UpdateOrderCmd
 *   <li><b>Modify</b>：部分修改资源，示例：ModifyCustomerStatusCmd
 *   <li><b>Add</b>：添加子资源，示例：AddOrderItemCmd、AddCustomerTagCmd
 *   <li><b>Delete</b>：删除资源，示例：DeleteCustomerCmd、DeleteOrderCmd
 *   <li><b>Remove</b>：移除子资源，示例：RemoveOrderItemCmd、RemoveCustomerTagCmd
 * </ul>
 *
 * <p><b>命名格式：</b>
 *
 * <ul>
 *   <li>{Verb}{Domain}Cmd
 *   <li>示例：CreateCustomerCmd、UpdateOrderCmd、AddOrderItemCmd
 * </ul>
 *
 * <p><b>类的职责：</b>
 *
 * <ul>
 *   <li>继承 com.alibaba.cola.dto.Command 基类
 *   <li>使用 @Data 和 @EqualsAndHashCode(callSuper = false) 注解
 *   <li>定义写操作所需的业务字段
 *   <li>使用 JSR 303 注解进行参数校验（@NotNull、@NotEmpty 等）
 * </ul>
 *
 * <p><b>使用场景：</b>
 *
 * <ul>
 *   <li>作为 Client 写操作方法的参数
 *   <li>在 Adapter 层由 Request 对象转换而来
 *   <li>在 App 层转换为 Domain 层的 Entity 对象
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>Command 对象只包含写操作需要的字段，不包含查询条件
 *   <li>字段命名应清晰表达业务含义
 *   <li>必须遵循动词映射表，保持命名一致性
 *   <li>参数校验应在 Command 对象上使用 JSR 303 注解
 *   <li>Command 对象应该是不可变的或使用 Builder 模式（可选）
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.mealmate.client.dto.cmd;
