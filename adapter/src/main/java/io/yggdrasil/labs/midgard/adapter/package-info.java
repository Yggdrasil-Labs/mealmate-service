/**
 * 适配层（Adapter Layer）
 *
 * <p>本包是 COLA 5.0 架构的 Adapter 层，负责将外部协议请求适配为应用层可执行的用例调用。
 *
 * <p><b>核心职责：</b>
 *
 * <ul>
 *   <li>接收外部请求并适配到内部用例入口（入站适配器）
 *   <li>处理不同协议的输入输出（当前示例包含 Web、mobile、wap）
 *   <li>将 Web Request 转换为 App 层可识别的 Command 或 Query
 *   <li>调用 App 层服务并返回结果
 * </ul>
 *
 * <p><b>骨架说明：</b>
 *
 * <ul>
 *   <li><b>web</b>：按领域组织 Web 适配代码，推荐结构为 `web.{domain}`
 *   <li><b>mobile</b>：移动端适配器骨架
 *   <li><b>wap</b>：WAP 端适配器骨架
 * </ul>
 *
 * <p><b>设计原则：</b>
 *
 * <ul>
 *   <li><b>协议转换</b>：将外部协议转换为内部统一接口调用
 *   <li><b>薄适配层</b>：只做协议转换和数据映射，不包含业务逻辑
 *   <li><b>参数校验</b>：在 Request 对象上使用 JSR 303 注解进行参数校验
 *   <li><b>异常处理</b>：统一处理异常并转换为合适的响应格式
 * </ul>
 *
 * <p><b>转换链路：</b>
 *
 * <pre>
 * HTTP Request (JSON)
 *   ↓ [Jackson 反序列化]
 * Request DTO (Adapter)
 *   ↓ [Convertor - MapStruct]
 * Command/Query (App)
 *   ↓ [调用 AppService]
 * Response/CO (App)
 *   ↓ [Jackson 序列化]
 * HTTP Response (JSON)
 * </pre>
 *
 * <p><b>依赖关系：</b>
 *
 * <ul>
 *   <li><b>依赖</b>：依赖 App 层服务和 DTO
 *   <li><b>不依赖</b>：不直接依赖 Domain 和 Infrastructure 层
 *   <li><b>被依赖</b>：Start 层依赖 Adapter 层（扫描 Controller 等）
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>Adapter 层只做适配转换，不包含业务逻辑
 *   <li>Controller 方法应简洁，只做参数转换和接口调用
 *   <li>使用 @Validated 和 @Valid 启用参数校验
 *   <li>Web 适配应优先按领域组织在 `web.{domain}` 下
 *   <li>不同的外部协议应保持各自独立，避免跨协议耦合
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.midgard.adapter;
