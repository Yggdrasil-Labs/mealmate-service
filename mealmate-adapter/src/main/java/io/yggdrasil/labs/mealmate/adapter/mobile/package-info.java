/**
 * 移动端适配层
 *
 * <p>本包属于 Adapter 层，负责处理移动端的 HTTP 请求。
 *
 * <p><b>职责：</b>
 *
 * <ul>
 *   <li>接收和处理移动端 HTTP 请求
 *   <li>移动端特定的参数校验和格式转换
 *   <li>调用应用层服务
 *   <li>返回移动端适配的响应格式
 * </ul>
 *
 * <p><b>使用场景：</b>
 *
 * <ul>
 *   <li>为移动端（iOS/Android）提供 RESTful API 接口
 *   <li>处理移动端特定的业务请求
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>本层为薄层，不包含业务逻辑，仅负责请求转发
 *   <li>移动端接口可能需要与 Web 端接口不同的数据格式或字段
 *   <li>考虑移动端的网络环境和性能要求
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.mealmate.adapter.mobile;
