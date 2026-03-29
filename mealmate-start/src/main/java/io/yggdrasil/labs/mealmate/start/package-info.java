/**
 * 启动层：仅包含启动类 & 框架配置
 *
 * <p>本包是 MealMate（mealmate-service）的启动层，遵循 DDD 分层架构原则。
 *
 * <p><b>包结构规范</b>：
 *
 * <ul>
 *   <li>启动类：<code>io.yggdrasil.labs.mealmate.start.Application</code>
 *   <li>全局配置：<code>io.yggdrasil.labs.mealmate.start.config.*</code>
 *   <li>全局AOP：<code>io.yggdrasil.labs.mealmate.start.aspect.*</code>
 *   <li>启动监听器：<code>io.yggdrasil.labs.mealmate.start.initializer.*</code>
 * </ul>
 *
 * <p><b>职责：</b>
 *
 * <ul>
 *   <li>定义 Spring Boot 应用启动类
 *   <li>配置应用扫描路径
 *   <li>提供应用启动入口
 *   <li>包含全局框架配置（非业务配置）
 * </ul>
 *
 * <p><b>架构原则：</b>
 *
 * <ul>
 *   <li>Start 层只能做"启动 + 配置"，不写业务逻辑
 *   <li>仅包含 SpringBoot 启动类和框架配置
 *   <li>不包含任何业务逻辑代码
 * </ul>
 *
 * <p><b>使用场景：</b>
 *
 * <ul>
 *   <li>应用启动和运行
 *   <li>配置 Spring Boot 相关设置
 *   <li>全局框架配置（WebMvc、CORS、拦截器、异常处理等）
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>启动类应使用 @SpringBootApplication 注解
 *   <li>确保扫描路径包含所有需要的包
 *   <li>启动类应保持简洁，复杂配置应在配置类中完成
 *   <li>不包含任何业务逻辑代码
 * </ul>
 *
 * <p>MealMate 业务后端（mealmate-service）基于 COLA 5.0 DDD 分层，本包仅提供启动与全局框架装配。
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.mealmate.start;
