package io.yggdrasil.labs.mealmate.domain.mealplan.repo;

import java.util.List;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItemHistory;

/** 餐计划条目调整历史仓储接口。 */
public interface MealPlanItemHistoryRepository {

    void save(MealPlanItemHistory history);

    List<MealPlanItemHistory> findByItemId(Long itemId);
}
