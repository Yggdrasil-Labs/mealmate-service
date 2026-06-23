package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ManualAddItemCmd;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.mealplan.exception.MealPlanErrorCode;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealPlanCrowdType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealType;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

/** 手动添加条目命令执行器（按菜名查找或仅记录名称）。 */
@Component
@RequiredArgsConstructor
public class ManualAddItemCmdExe {

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final RecipeRepository recipeRepository;

    @Transactional(rollbackFor = Exception.class)
    public void execute(@Valid ManualAddItemCmd cmd) {
        assertPlanDraft(cmd.getPlanId());
        // recipe 未找到时使用占位 ID 0 而非 null（DDL NOT NULL 约束）
        Long recipeId = 0L;
        Optional<Recipe> opt = recipeRepository.findByName(cmd.getRecipeName());
        if (opt.isPresent()) {
            recipeId = opt.get().getId();
        }
        MealPlanItem item =
                MealPlanItem.builder()
                        .planId(cmd.getPlanId())
                        .recipeId(recipeId)
                        .mealDate(cmd.getMealDate())
                        .mealType(
                                cmd.getMealType() != null
                                        ? MealType.valueOf(cmd.getMealType())
                                        : MealType.LUNCH)
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
