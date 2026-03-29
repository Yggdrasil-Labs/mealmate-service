/**
 * Repository 实现（Repository Implementation）
 *
 * <p>本包实现 Domain 层定义的 Repository 接口，提供数据持久化的具体实现。
 *
 * <p><b>核心职责：</b>
 *
 * <ul>
 *   <li>实现 Domain 层定义的 Repository 接口
 *   <li>使用 MyBatis-Plus Mapper 进行数据库操作
 *   <li>使用 Convertor 进行 DO ↔ Entity 转换
 *   <li>处理数据库异常并转换为领域异常
 * </ul>
 *
 * <p><b>设计原则：</b>
 *
 * <ul>
 *   <li><b>依赖倒置</b>：实现 Domain 层定义的接口，Domain 不依赖 Infrastructure
 *   <li><b>对象转换</b>：DO 与 Entity 通过 Convertor 转换，保持两者独立
 *   <li><b>异常转换</b>：将数据库异常转换为领域异常
 *   <li><b>单一职责</b>：Repository 实现只负责数据持久化，不包含业务逻辑
 * </ul>
 *
 * <p><b>Repository 实现示例：</b>
 *
 * <pre>{@code
 * @Repository
 * public class CustomerRepositoryImpl implements CustomerRepository {
 *
 *     @Resource
 *     private CustomerMapper customerMapper;  // 由 @AutoMybatis 自动生成
 *
 *     @Resource
 *     private CustomerInfraConvertor customerInfraConvertor;
 *
 *     @Override
 *     public Customer findById(String customerId) {
 *         CustomerDO customerDO = customerMapper.selectById(customerId);
 *         if (customerDO == null) {
 *             return null;
 *         }
 *         return customerInfraConvertor.toEntity(customerDO);
 *     }
 *
 *     @Override
 *     public List<Customer> findByNameLike(String name) {
 *         LambdaQueryWrapper<CustomerDO> wrapper = new LambdaQueryWrapper<>();
 *         wrapper.like(CustomerDO::getCustomerName, name);
 *         List<CustomerDO> customerDOList = customerMapper.selectList(wrapper);
 *         return customerDOList.stream()
 *                 .map(customerInfraConvertor::toEntity)
 *                 .collect(Collectors.toList());
 *     }
 *
 *     @Override
 *     public void save(Customer customer) {
 *         CustomerDO customerDO = customerInfraConvertor.toDO(customer);
 *         customerMapper.insert(customerDO);
 *     }
 *
 *     @Override
 *     public void update(Customer customer) {
 *         CustomerDO customerDO = customerInfraConvertor.toDO(customer);
 *         customerMapper.updateById(customerDO);
 *     }
 *
 *     @Override
 *     public void delete(String customerId) {
 *         customerMapper.deleteById(customerId);
 *     }
 *
 *     @Override
 *     public boolean existsByCompanyName(String companyName) {
 *         LambdaQueryWrapper<CustomerDO> wrapper = new LambdaQueryWrapper<>();
 *         wrapper.eq(CustomerDO::getCompanyName, companyName);
 *         return customerMapper.selectCount(wrapper) > 0;
 *     }
 * }
 * }</pre>
 *
 * <p><b>命名规范：</b>
 *
 * <ul>
 *   <li>实现类命名：{Domain}RepositoryImpl（如 CustomerRepositoryImpl）
 *   <li>包名：infra.persistence.impl
 *   <li>对应的接口：domain.{aggregate}.repo.{Domain}Repository
 *   <li>使用 @Repository 注解标注为 Spring Bean
 * </ul>
 *
 * <p><b>依赖关系：</b>
 *
 * <ul>
 *   <li><b>实现</b>：实现 domain.repo 中定义的 Repository 接口
 *   <li><b>依赖</b>：依赖 Mapper（由 @AutoMybatis 生成）和 Convertor
 *   <li><b>被依赖</b>：被 App 层通过 Repository 接口依赖（依赖倒置）
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>Repository 实现只负责数据持久化，不包含业务逻辑
 *   <li>业务逻辑应在 Domain 层的 Entity 或 DomainService 中实现
 *   <li>使用 LambdaQueryWrapper 构建查询条件，避免硬编码字段名
 *   <li>返回 null 表示未找到，不抛出异常（由上层决定如何处理）
 *   <li>数据库异常可以转换为领域异常，避免技术细节泄露
 *   <li>复杂查询可以使用 MyBatis XML 方式实现
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.mealmate.infrastructure.persistence.impl;
