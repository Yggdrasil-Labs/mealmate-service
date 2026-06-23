package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import jakarta.validation.Valid;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AddItemCmd;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.mealplan.exception.MealPlanErrorCode;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealPlanCrowdType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealType;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import lombok.RequiredArgsConstructor;

/** 添加计划条目命令执行器。 */
@Component
@RequiredArgsConstructor
public class AddItemCmdExe {

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;

    @Transactional(rollbackFor = Exception.class)
    public void execute(@Valid AddItemCmd cmd) {
        assertPlanDraft(cmd.getPlanId());
        MealPlanItem item =
                MealPlanItem.builder()
                        .planId(cmd.getPlanId())
                        .recipeId(cmd.getRecipeId())
                        .mealDate(cmd.getMealDate())
                        .mealType(MealType.valueOf(cmd.getMealType()))
                        .crowdType(
                                cmd.getCrowdType() != null
                                        ? MealPlanCrowdType.valueOf(cmd.getCrowdType())
                                        : MealPlanCrowdType.FAMILY)
                        .sortOrder(0)
                        .build();
        weeklyMealPlanRepository.saveItem(item);
    }

    private void assertPlanDraft(Long planId) {
        WeeklyMealPlan plan = assertPlanOwnership(planId);
        plan.assertDraft();
    }

    private WeeklyMealPlan assertPlanOwnership(Long planId) {
        return weeklyMealPlanRepository
                .findById(planId)
                .orElseThrow(() -> new BizException(MealPlanErrorCode.PLAN_NOT_FOUND));
    }
}
