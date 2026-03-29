/**
 * Web 适配层
 *
 * <p>本包负责 HTTP 协议适配，是 Adapter 层对外暴露 Web 能力的入口。
 *
 * <p><b>骨架说明：</b>
 *
 * <ul>
 *   <li>新项目应按领域在 <b>web.{domain}</b> 下创建 Controller、Request DTO 和 Web Convertor
 * </ul>
 *
 * <p><b>核心职责：</b>
 *
 * <ul>
 *   <li>接收 HTTP 请求并完成参数校验
 *   <li>将 Web 请求 DTO 转换为应用层可识别的 Command/Query
 *   <li>调用应用层服务完成用例编排
 *   <li>返回统一格式的响应给前端
 * </ul>
 *
 * <p><b>设计原则：</b>
 *
 * <ul>
 *   <li><b>薄 Controller</b>：只做协议转换，不承载业务逻辑
 *   <li><b>RESTful 风格</b>：接口路径、HTTP 方法和返回值保持资源语义
 *   <li><b>统一校验</b>：使用 @Validated 和 @Valid 做入参校验
 *   <li><b>统一异常</b>：由全局异常处理器统一处理错误
 * </ul>
 *
 * <p><b>依赖关系：</b>
 *
 * <ul>
 *   <li><b>依赖</b>：依赖应用层服务、请求 DTO 和对象转换器
 *   <li><b>不依赖</b>：不直接依赖 Domain 或 Infrastructure 层
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.mealmate.adapter.web;
