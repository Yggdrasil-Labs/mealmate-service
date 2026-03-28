/**
 * 基础设施层（Infrastructure Layer）
 *
 * <p>本包是 COLA 5.0 架构的 Infrastructure 层，负责所有技术实现和外部依赖。
 *
 * <p><b>核心职责：</b>
 *
 * <ul>
 *   <li>实现 Domain 层定义的 Repository 和 Gateway 接口
 *   <li>提供数据库访问、缓存、消息队列等技术实现
 *   <li>封装第三方服务调用（HTTP、RPC）
 *   <li>提供技术配置和工具类
 * </ul>
 *
 * <p><b>包结构：</b>
 *
 * <ul>
 *   <li><b>persistence</b>：数据持久化
 *       <ul>
 *         <li>dataobject：数据库对象（DO）
 *         <li>mapper：MyBatis Mapper（由 @AutoMybatis 生成）
 *         <li>convertor：DO ↔ Entity 转换器
 *         <li>impl：Repository 接口实现
 *       </ul>
 *   <li><b>gateway</b>：外部服务调用（Gateway 实现）
 *   <li><b>cache</b>：缓存实现（Redis、本地缓存）
 *   <li><b>mq</b>：消息队列实现（Kafka、RocketMQ）
 *   <li><b>config</b>：技术配置（MyBatis、Redis、MQ 等）
 *   <li><b>file</b>：文件存储实现（OSS、MinIO）
 *   <li><b>scheduler</b>：定时任务实现
 *   <li><b>util</b>：技术工具类
 * </ul>
 *
 * <p><b>设计原则：</b>
 *
 * <ul>
 *   <li><b>依赖倒置</b>：实现 Domain 层定义的接口，Domain 不依赖 Infrastructure
 *   <li><b>技术隔离</b>：封装所有技术细节，避免技术泄露到上层
 *   <li><b>对象转换</b>：DO（数据库对象）与 Entity（领域对象）相互独立，通过 Convertor 转换
 *   <li><b>异常转换</b>：将技术异常转换为领域异常
 * </ul>
 *
 * <p><b>Repository 实现模式：</b>
 *
 * <pre>{@code
 * @Repository
 * public class CustomerRepositoryImpl implements CustomerRepository {
 *
 *     @Resource
 *     private CustomerMapper customerMapper;  // 由 @AutoMybatis 生成
 *
 *     @Resource
 *     private CustomerInfraConvertor customerInfraConvertor;    // DO ↔ Entity 转换
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
 * <p><b>Gateway 实现模式：</b>
 *
 * <pre>{@code
 * @Component
 * public class PaymentGatewayImpl implements PaymentGateway {
 *
 *     @Resource
 *     private RestTemplate restTemplate;
 *
 *     @Override
 *     public PaymentResult pay(PaymentRequest request) {
 *         // 调用第三方支付接口
 *         String url = "https://api.payment.com/pay";
 *         PaymentResponse response = restTemplate.postForObject(url, request, PaymentResponse.class);
 *         // 转换为领域对象
 *         return convertToPaymentResult(response);
 *     }
 * }
 * }</pre>
 *
 * <p><b>依赖关系：</b>
 *
 * <ul>
 *   <li><b>依赖</b>：依赖 Domain 层接口（Repository、Gateway）
 *   <li><b>不依赖</b>：不依赖 Client、Adapter、App 层
 *   <li><b>被依赖</b>：被 App 层通过 Domain 接口依赖（依赖倒置）
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>Infrastructure 层只提供技术实现，不包含业务逻辑
 *   <li>DO 对象与 Entity 对象应保持独立，通过 Convertor 转换
 *   <li>使用 @AutoMybatis 注解自动生成 MyBatis Mapper 和 Service
 *   <li>技术异常应转换为领域异常，避免技术细节泄露
 *   <li>Repository 实现类命名：{Domain}RepositoryImpl
 *   <li>Gateway 实现类命名：{External}GatewayImpl
 * </ul>
 *
 * @author YoungerYang-Y
 */
package io.yggdrasil.labs.midgard.infrastructure;
