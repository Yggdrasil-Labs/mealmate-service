/**
 * 客户端接口定义（Client API）
 *
 * <p>本包属于 Client 层，定义应用对外提供的业务接口。
 *
 * <p><b>职责：</b>
 *
 * <ul>
 *   <li>定义聚合根的业务接口（Client）
 *   <li>声明写操作接口（Command 方法）
 *   <li>声明读操作接口（Query 方法）
 *   <li>作为 Adapter 层调用 App 层的统一入口
 * </ul>
 *
 * <p><b>类命名规则：</b>
 *
 * <ul>
 *   <li>命名格式：{Domain}Client
 *   <li>示例：CustomerClient、OrderClient、ProductClient
 *   <li>按聚合根（Aggregate）组织，一个聚合根一个 Client
 * </ul>
 *
 * <p><b>方法命名规则：</b>
 *
 * <ul>
 *   <li>写操作：{verb}{Domain}，参数为 {Verb}{Domain}Cmd
 *   <li>示例：createCustomer(CreateCustomerCmd)、updateOrder(UpdateOrderCmd)
 *   <li>读操作：{verb}{Domain}，参数为 {Verb}{Domain}Qry
 *   <li>示例：listCustomerByName(ListCustomerQry)、getOrderById(GetOrderQry)
 *   <li>返回类型：写操作返回 Response，读操作返回 Response&lt;CO&gt; 或 MultiResponse&lt;CO&gt;
 * </ul>
 *
 * <p><b>使用场景：</b>
 *
 * <ul>
 *   <li>Adapter 层的 Controller 调用 Client 接口
 *   <li>App 层实现 Client 接口（如 CustomerClientImpl）
 *   <li>作为系统对外提供的业务能力边界
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>Client 接口只定义方法签名，不包含实现
 *   <li>接口方法应清晰表达业务意图，方法名要有业务语义
 *   <li>一个 Client 对应一个聚合根，保持接口内聚
 *   <li>接口变更需要谨慎，影响所有调用方和实现方
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.midgard.client.api;
