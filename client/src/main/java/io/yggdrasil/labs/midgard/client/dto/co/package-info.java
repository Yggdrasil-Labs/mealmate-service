/**
 * 客户对象（Client Object，CO）
 *
 * <p>本包属于 Client 层的 DTO 模块，定义读操作（Query）的输出结果。
 *
 * <p><b>职责：</b>
 *
 * <ul>
 *   <li>定义读操作的返回数据结构
 *   <li>封装业务数据，用于返回给调用方
 *   <li>作为 Facade 读操作方法的返回值（Response&lt;CO&gt; 或 MultiResponse&lt;CO&gt;）
 * </ul>
 *
 * <p><b>类命名规则：</b>
 *
 * <ul>
 *   <li>命名格式：{Domain}CO
 *   <li>示例：CustomerCO、OrderCO、ProductCO
 *   <li>按聚合根（Aggregate）组织，一个聚合根一个 CO
 * </ul>
 *
 * <p><b>类的职责：</b>
 *
 * <ul>
 *   <li>继承 com.alibaba.cola.dto.DTO 基类
 *   <li>使用 @Data 和 @EqualsAndHashCode(callSuper = true) 注解
 *   <li>定义返回给调用方的业务字段
 *   <li>只包含需要展示给外部的字段，不包含内部实现细节
 * </ul>
 *
 * <p><b>使用场景：</b>
 *
 * <ul>
 *   <li>作为 Client 读操作方法的返回值类型
 *   <li>在 App 层由 Domain Entity 转换而来
 *   <li>在 Adapter 层直接返回给前端
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>CO 对象只包含需要展示给外部的字段，隐藏内部实现
 *   <li>字段命名应清晰表达业务含义
 *   <li>避免在 CO 中包含敏感信息（如密码、内部 ID 等）
 *   <li>CO 对象应该是只读的，不用于写操作
 *   <li>可以包含计算字段或格式化后的字段（如格式化后的日期）
 *   <li>与 Domain Entity 保持独立，允许有不同的字段结构
 * </ul>
 *
 * <p><b>与 VO 的区别：</b>
 *
 * <ul>
 *   <li><b>CO</b>：Client Object，用于 Client 层，是应用层对外展示的对象
 *   <li><b>VO</b>：Value Object，用于 Domain 层，是领域模型中的值对象
 *   <li>CO 关注外部展示，VO 关注领域概念
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.midgard.client.dto.co;
