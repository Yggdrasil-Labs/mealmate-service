package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import jakarta.validation.Valid;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ReplaceItemCmd;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.mealplan.exception.MealPlanErrorCode;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import lombok.RequiredArgsConstructor;

/** 替换计划条目菜品命令执行器。 */
@Component
@RequiredArgsConstructor
public class ReplaceItemCmdExe {

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;

    @Transactional(rollbackFor = Exception.class)
    public void execute(@Valid ReplaceItemCmd cmd) {
        assertPlanDraft(cmd.getPlanId());
        MealPlanItem item =
                weeklyMealPlanRepository
                        .findItemById(cmd.getItemId())
                        .orElseThrow(() -> new BizException(MealPlanErrorCode.ITEM_NOT_FOUND));
        if (!cmd.getPlanId().equals(item.getPlanId())) {
            throw new BizException(MealPlanErrorCode.ITEM_NOT_FOUND);
        }
        item.setRecipeId(cmd.getRecipeId());
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
