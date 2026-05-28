package io.yggdrasil.labs.mealmate.domain.mealplan.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.ShoppingItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;

/** 周计划仓储接口。 */
public interface WeeklyMealPlanRepository {

    WeeklyMealPlan save(WeeklyMealPlan plan);

    Optional<WeeklyMealPlan> findById(Long planId);

    Optional<WeeklyMealPlan> findByIdWithItems(Long planId);

    Optional<WeeklyMealPlan> findByFamilyIdAndWeekStartDate(Long familyId, LocalDate weekStartDate);

    /** 带行锁查询，仅在写操作（覆盖旧 DRAFT）中使用。 */
    Optional<WeeklyMealPlan> findByFamilyIdAndWeekStartDateForUpdate(
            Long familyId, LocalDate weekStartDate);

    void updateStatus(Long planId, PlanStatus status);

    void logicalDelete(Long planId);

    void deleteItemsByPlanId(Long planId);

    MealPlanItem saveItem(MealPlanItem item);

    void deleteItem(Long itemId);

    Optional<MealPlanItem> findItemById(Long itemId);

    List<ShoppingItem> findShoppingItemsByPlanId(Long planId);

    void saveShoppingItems(Long planId, List<ShoppingItem> items);

    void updateShoppingItemPurchased(Long itemId, boolean purchased);
}
