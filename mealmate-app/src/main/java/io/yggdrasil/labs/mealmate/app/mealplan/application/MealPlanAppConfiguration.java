package io.yggdrasil.labs.mealmate.app.mealplan.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.yggdrasil.labs.mealmate.domain.mealplan.service.DuplicateCheckDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.IngredientFilterDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.PrepPlanDeriveDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.WeekPlanGenerateDomainService;

/**
 * 周餐计划应用层装配。
 *
 * <p>将领域服务注册为 Spring Bean，保持领域层无框架依赖。
 */
@Configuration(proxyBeanMethods = false)
public class MealPlanAppConfiguration {

    @Bean
    public IngredientFilterDomainService ingredientFilterDomainService() {
        return new IngredientFilterDomainService();
    }

    @Bean
    public DuplicateCheckDomainService duplicateCheckDomainService() {
        return new DuplicateCheckDomainService();
    }

    @Bean
    public WeekPlanGenerateDomainService weekPlanGenerateDomainService() {
        return new WeekPlanGenerateDomainService();
    }

    @Bean
    public PrepPlanDeriveDomainService prepPlanDeriveDomainService() {
        return new PrepPlanDeriveDomainService();
    }
}
