/**
 * 数据库对象（Data Object）
 *
 * <p>本包定义数据库对象（DO），对应数据库表结构。
 *
 * <p><b>核心职责：</b>
 *
 * <ul>
 *   <li>定义数据库表的 Java 映射对象
 *   <li>使用 MyBatis-Plus 注解标注表名和字段映射
 *   <li>使用 @AutoMybatis 注解自动生成 Mapper 和 Service
 * </ul>
 *
 * <p><b>设计原则：</b>
 *
 * <ul>
 *   <li><b>纯数据对象</b>：DO 只包含数据字段，不包含业务逻辑
 *   <li><b>与 Entity 分离</b>：DO 关注数据库表结构，Entity 关注领域模型
 *   <li><b>自动生成 Mapper</b>：使用 @AutoMybatis 注解自动生成 MyBatis-Plus Mapper
 *   <li><b>驼峰命名</b>：Java 字段使用驼峰命名，数据库字段使用下划线命名
 * </ul>
 *
 * <p><b>DO 定义示例：</b>
 *
 * <pre>{@code
 * @Data
 * @TableName("customer")
 * @AutoMybatis  // 编译期自动生成 CustomerMapper 和 CustomerService
 * public class CustomerDO {
 *
 *     // 主键
 *     @TableId(type = IdType.INPUT)
 *     private String customerId;
 *
 *     // 会员ID
 *     private String memberId;
 *
 *     // 客户名称
 *     private String customerName;
 *
 *     // 客户类型（ENTERPRISE, PERSONAL）
 *     private String customerType;
 *
 *     // 公司名称
 *     private String companyName;
 *
 *     // 来源（WEB, APP, API, WECHAT）
 *     private String source;
 *
 *     // 创建时间
 *     @TableField(fill = FieldFill.INSERT)
 *     private LocalDateTime createTime;
 *
 *     // 更新时间
 *     @TableField(fill = FieldFill.INSERT_UPDATE)
 *     private LocalDateTime updateTime;
 * }
 * }</pre>
 *
 * <p><b>@AutoMybatis 注解：</b>
 *
 * <ul>
 *   <li>编译期自动生成 {Domain}Mapper 接口（如 CustomerMapper）
 *   <li>编译期自动生成 {Domain}Service 类（如 CustomerService）
 *   <li>Mapper 继承 MyBatis-Plus 的 BaseMapper，提供基础 CRUD 操作
 *   <li>Service 继承 MyBatis-Plus 的 ServiceImpl，提供批量操作等扩展功能
 * </ul>
 *
 * <p><b>常用注解：</b>
 *
 * <ul>
 *   <li><b>@TableName</b>：指定数据库表名
 *   <li><b>@TableId</b>：标注主键字段，type 指定主键生成策略
 *   <li><b>@TableField</b>：标注字段映射，fill 指定自动填充策略
 *   <li><b>@TableLogic</b>：标注逻辑删除字段
 *   <li><b>@Version</b>：标注乐观锁版本字段
 * </ul>
 *
 * <p><b>命名规范：</b>
 *
 * <ul>
 *   <li>DO 类命名：{Domain}DO（如 CustomerDO、OrderDO）
 *   <li>包名：infra.persistence.dataobject
 *   <li>对应的 Entity：domain.{aggregate}.model.{Domain}（如 Customer）
 *   <li>对应的 Mapper：由 @AutoMybatis 自动生成（无需手动创建）
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>DO 只关注数据库表结构，不包含业务逻辑
 *   <li>DO 与 Entity 应保持独立，通过 Convertor 转换
 *   <li>枚举类型在 DO 中使用 String 存储，在 Entity 中使用 Enum 类型
 *   <li>日期类型推荐使用 LocalDateTime 而非 Date
 *   <li>使用 @AutoMybatis 后无需手动创建 Mapper 接口
 *   <li>DO 类应使用 @Data 注解，避免手动编写 getter/setter
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.midgard.infrastructure.persistence.dataobject;
