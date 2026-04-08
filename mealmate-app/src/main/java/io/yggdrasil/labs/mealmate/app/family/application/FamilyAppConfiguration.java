package io.yggdrasil.labs.mealmate.app.family.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.yggdrasil.labs.mealmate.domain.family.service.FamilyDomainService;

/**
 * 家庭 UC 的应用层装配。
 *
 * <p>领域服务保持 POJO（不引入 Spring 注解），通过应用层配置统一托管为单例 Bean， 便于 Executor 复用与测试替换，同时不污染领域层框架依赖。
 */
@Configuration(proxyBeanMethods = false)
public class FamilyAppConfiguration {

    /** 注册无状态领域服务单例。 */
    @Bean
    public FamilyDomainService familyDomainService() {
        return new FamilyDomainService();
    }
}
