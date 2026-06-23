package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.mealplan.exception.MealPlanErrorCode;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;

/** Executor 共享的计划校验工具。 */
public final class MealPlanHelper {

    private MealPlanHelper() {}

    /** 校验 planId 存在并返回计划。 */
    public static WeeklyMealPlan assertPlanExists(WeeklyMealPlanRepository repo, Long planId) {
        return repo.findById(planId)
                .orElseThrow(() -> new BizException(MealPlanErrorCode.PLAN_NOT_FOUND));
    }

    /** 校验计划处于 DRAFT 状态（可编辑）。 */
    public static void assertPlanDraft(WeeklyMealPlanRepository repo, Long planId) {
        WeeklyMealPlan plan = assertPlanExists(repo, planId);
        plan.assertDraft();
    }

    /** 获取 familyId，null 时抛异常。 */
    public static Long requireFamilyId(Long familyId) {
        if (familyId != null) {
            return familyId;
        }
        throw new BizException(MealPlanErrorCode.FAMILY_ID_REQUIRED);
    }
}
