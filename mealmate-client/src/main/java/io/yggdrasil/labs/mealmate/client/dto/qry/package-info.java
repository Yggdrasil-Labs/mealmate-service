/**
 * 查询对象（Qry）
 *
 * <p>本包属于 Client 层的 DTO 模块，定义读操作（Query）的输入参数。
 *
 * <p><b>职责：</b>
 *
 * <ul>
 *   <li>定义读操作（查询、列表、分页等）的输入参数
 *   <li>封装查询条件和筛选条件
 *   <li>作为 Facade 读操作方法的参数
 * </ul>
 *
 * <p><b>类命名规则：</b>
 *
 * <p>遵循 COLA 5.0 命名规范，使用固定动词映射表：
 *
 * <ul>
 *   <li><b>Get</b>：获取单个资源，示例：GetCustomerQry、GetOrderQry
 *   <li><b>Query</b>：复杂查询，示例：QueryCustomerQry、QueryOrderQry
 *   <li><b>List</b>：列表查询，示例：ListCustomerQry、ListOrderQry
 *   <li><b>Page</b>：分页查询，示例：PageCustomerQry、PageOrderQry
 *   <li><b>Check</b>：检查操作，示例：CheckCustomerExistsQry
 * </ul>
 *
 * <p><b>命名格式：</b>
 *
 * <ul>
 *   <li>{Verb}{Domain}Qry
 *   <li>示例：GetCustomerQry、ListCustomerQry、PageOrderQry
 * </ul>
 *
 * <p><b>类的职责：</b>
 *
 * <ul>
 *   <li>继承 com.alibaba.cola.dto.Query 基类
 *   <li>使用 @Data 和 @EqualsAndHashCode(callSuper = true) 注解
 *   <li>定义查询条件和筛选字段
 *   <li>分页查询应包含分页参数（pageNum、pageSize 等）
 * </ul>
 *
 * <p><b>使用场景：</b>
 *
 * <ul>
 *   <li>作为 Client 读操作方法的参数
 *   <li>在 Adapter 层由 Request 对象转换而来
 *   <li>在 App 层用于构建查询条件
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>Query 对象只包含查询条件，不包含业务数据
 *   <li>字段命名应清晰表达查询意图
 *   <li>必须遵循动词映射表，保持命名一致性
 *   <li>分页查询应使用 PageQuery 或包含分页字段
 *   <li>查询条件字段应该是可选的，使用包装类型（如 String、Integer）
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.mealmate.client.dto.qry;
