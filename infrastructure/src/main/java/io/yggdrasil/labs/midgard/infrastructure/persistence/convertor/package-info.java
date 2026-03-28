/**
 * Infrastructure 层对象转换器
 *
 * <p>负责 DO ↔ Entity 的转换
 *
 * <p><b>职责：</b>
 *
 * <ul>
 *   <li>实现数据库对象（DO）与领域实体（Entity）之间的转换
 *   <li>使用 MapStruct 自动生成转换代码
 *   <li>保持转换逻辑简单，不包含业务逻辑
 * </ul>
 *
 * <p><b>命名规范：</b>
 *
 * <ul>
 *   <li>接口命名：{Domain}Convertor
 *   <li>示例：CustomerInfraConvertor、OrderInfraConvertor
 * </ul>
 *
 * <p><b>注意事项：</b>
 *
 * <ul>
 *   <li>DO 和 Entity 的字段映射应该尽可能简单
 *   <li>复杂的类型转换使用 default 方法实现
 *   <li>不要在 Convertor 中包含业务逻辑
 * </ul>
 */
package io.yggdrasil.labs.midgard.infrastructure.persistence.convertor;
