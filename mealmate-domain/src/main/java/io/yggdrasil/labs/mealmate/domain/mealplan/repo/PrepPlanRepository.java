package io.yggdrasil.labs.mealmate.domain.mealplan.repo;

import java.util.Optional;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PrepTaskStatus;

/** 备菜计划仓储接口。 */
public interface PrepPlanRepository {

    PrepPlan save(PrepPlan prepPlan);

    Optional<PrepPlan> findByPlanId(Long planId);

    void updateItemStatus(Long itemId, PrepTaskStatus status);
}
