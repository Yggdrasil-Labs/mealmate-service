package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AdjustMealItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.MealPlanItemCO;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.mealplan.exception.MealPlanErrorCode;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItemHistory;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.AdjustReason;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.MealPlanItemHistoryRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.MealPlanRuleDomainService;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

/** 替换餐次菜品命令执行器。 */
@Component
@RequiredArgsConstructor
public class AdjustMealItemCmdExe {

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final RecipeRepository recipeRepository;
    private final MealPlanItemHistoryRepository historyRepository;
    private final MealPlanRuleDomainService ruleDomainService;

    /** 执行菜品替换，记录调整历史并返回更新后的条目。 */
    public MealPlanItemCO execute(AdjustMealItemCmd cmd) {
        // 1. 加载计划
        WeeklyMealPlan plan =
                weeklyMealPlanRepository
                        .findByIdWithItems(cmd.getPlanId())
                        .orElseThrow(() -> new BizException(MealPlanErrorCode.PLAN_NOT_FOUND));

        // 2. 校验计划状态为 DRAFT
        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw new BizException(MealPlanErrorCode.PLAN_FROZEN);
        }

        // 3. 定位条目
        MealPlanItem item =
                plan.getItems().stream()
                        .filter(i -> i.getId().equals(cmd.getItemId()))
                        .findFirst()
                        .orElseThrow(() -> new BizException(MealPlanErrorCode.ITEM_NOT_FOUND));

        // 4. 校验新菜品存在
        Recipe recipe =
                recipeRepository
                        .findById(cmd.getNewRecipeId())
                        .orElseThrow(() -> new BizException(MealPlanErrorCode.ITEM_NOT_FOUND));

        // 5. 校验不重复
        ruleDomainService.validateNoDuplicate(
                plan.getItems(), cmd.getItemId(), cmd.getNewRecipeId());

        // 6-8. 执行替换并持久化
        Long oldRecipeId = item.getRecipeId();
        item.adjust(cmd.getNewRecipeId());
        weeklyMealPlanRepository.saveItem(item);

        // 9. 记录调整历史
        historyRepository.save(
                MealPlanItemHistory.builder()
                        .itemId(item.getId())
                        .planId(plan.getId())
                        .oldRecipeId(oldRecipeId)
                        .newRecipeId(cmd.getNewRecipeId())
                        .adjustReason(parseReason(cmd.getAdjustReason()))
                        .adjustedAt(LocalDateTime.now())
                        .adjustedBy(0L)
                        .build());

        // 10. 组装返回
        return MealPlanItemCO.builder()
                .itemId(item.getId())
                .recipeId(item.getRecipeId())
                .recipeName(recipe.getName())
                .crowdType(item.getCrowdType() != null ? item.getCrowdType().name() : null)
                .isWeightLoss(item.isWeightLoss())
                .isBabyMeal(item.isBabyMeal())
                .duplicateFlag(item.isDuplicateFlag())
                .coverImageUrl(recipe.getCoverImageUrl())
                .cookingTimeMin(recipe.getCookingTimeMin())
                .sortOrder(item.getSortOrder())
                .manuallyAdjusted(item.isManuallyAdjusted())
                .adjustCount(item.getAdjustCount())
                .build();
    }

    private AdjustReason parseReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return AdjustReason.OTHER;
        }
        try {
            return AdjustReason.valueOf(reason);
        } catch (IllegalArgumentException e) {
            return AdjustReason.OTHER;
        }
    }
}
