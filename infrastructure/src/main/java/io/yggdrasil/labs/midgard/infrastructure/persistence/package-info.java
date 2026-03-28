/**
 * 数据持久化（Persistence）
 *
 * <p>本包负责数据持久化的所有实现，包括数据库访问、对象转换和 Repository 实现。
 *
 * <p><b>核心职责：</b>
 *
 * <ul>
 *   <li>实现 Domain 层定义的 Repository 接口
 *   <li>定义数据库对象（DO）
 *   <li>提供 DO ↔ Entity 的转换器
 *   <li>使用 MyBatis-Plus 进行数据库操作
 * </ul>
 *
 * <p><b>包结构：</b>
 *
 * <ul>
 *   <li><b>dataobject</b>：数据库对象（DO），对应数据库表结构
 *   <li><b>mapper</b>：MyBatis Mapper 接口（由 @AutoMybatis 注解自动生成）
 *   <li><b>convertor</b>：DO ↔ Entity 转换器（使用 MapStruct）
 *   <li><b>impl</b>：Repository 接口实现类
 * </ul>
 *
 * <p><b>设计原则：</b>
 *
 * <ul>
 *   <li><b>对象分离</b>：DO（数据库对象）与 Entity（领域对象）相互独立
 *   <li><b>自动生成</b>：使用 @AutoMybatis 注解自动生成 Mapper 和 Service
 *   <li><b>转换器</b>：使用 MapStruct 自动生成转换代码
 *   <li><b>单一职责</b>：Repository 实现只负责数据持久化，不包含业务逻辑
 * </ul>
 *
 * <p><b>DO 定义示例：</b>
 *
 * <pre>{@code
 * @Data
 * @TableName("customer")
 * @AutoMybatis  // 自动生成 CustomerMapper 和 CustomerService
 * public class CustomerDO {
 *
 *     @TableId(type = IdType.INPUT)
 *     private String customerId;
 *
 *     private String customerName;
 *
 *     private String customerType;
 *
 *     // ... 其他字段
 * }
 * }</pre>
 *
 * <p><b>Convertor 定义示例：</b>
 *
 * <pre>{@code
 * @Mapper(componentModel = "spring")
 * public interface CustomerInfraConvertor {
 *
 *     CustomerInfraConvertor INSTANCE = Mappers.getMapper(CustomerInfraConvertor.class);
 *
 *     Customer toEntity(CustomerDO customerDO);
 *
 *     CustomerDO toDO(Customer customer);
 * }
 * }</pre>
 *
 * <p><b>Repository 实现示例：</b>
 *
 * <pre>{@code
 * @Repository
 * public class CustomerRepositoryImpl implements CustomerRepository {
 *
 *     @Resource
 *     private CustomerMapper customerMapper;  // 由 @AutoMybatis 生成
 *
 *     @Resource
 *     private CustomerInfraConvertor customerInfraConvertor;
 *
 *     @Override
 *     public Customer findById(String customerId) {
 *         CustomerDO customerDO = customerMapper.selectById(customerId);
 *         return customerInfraConvertor.toEntity(customerDO);
 *     }
 *
 *     @Override
 *     public void save(Customer customer) {
 *         CustomerDO customerDO = customerInfraConvertor.toDO(customer);
 *         customerMapper.insert(customerDO);
 *     }
 * }
 * }</pre>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>DO 类命名：{Domain}DO（如 CustomerDO）
 *   <li>Convertor 接口命名：{Domain}Convertor（如 CustomerInfraConvertor）
 *   <li>Repository 实现类命名：{Domain}RepositoryImpl（如 CustomerRepositoryImpl）
 *   <li>DO 只关注数据库表结构，不包含业务逻辑
 *   <li>Entity 只关注领域模型，不包含数据库细节
 *   <li>Convertor 负责两者之间的映射转换
 *   <li>@AutoMybatis 会在编译期生成 Mapper 和 Service
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.midgard.infrastructure.persistence;
