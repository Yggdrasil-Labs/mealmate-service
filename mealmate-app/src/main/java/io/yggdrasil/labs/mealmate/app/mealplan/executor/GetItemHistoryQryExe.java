package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.MealPlanItemHistoryCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetItemHistoryQry;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItemHistory;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.MealPlanItemHistoryRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

/** 调整历史查询执行器。 */
@Component
@RequiredArgsConstructor
public class GetItemHistoryQryExe {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final MealPlanItemHistoryRepository historyRepository;
    private final RecipeRepository recipeRepository;

    public List<MealPlanItemHistoryCO> execute(GetItemHistoryQry qry) {
        // 1. 校验条目存在且属于指定计划（防 IDOR）
        var item =
                weeklyMealPlanRepository
                        .findItemById(qry.getItemId())
                        .orElseThrow(
                                () -> new IllegalArgumentException("MEAL_PLAN_ITEM_NOT_FOUND"));
        if (!item.getPlanId().equals(qry.getPlanId())) {
            throw new IllegalArgumentException("MEAL_PLAN_ITEM_NOT_FOUND");
        }

        // 2. 查询历史记录
        List<MealPlanItemHistory> histories = historyRepository.findByItemId(qry.getItemId());

        // 3. 转为 CO，查询菜品名称
        return histories.stream()
                .map(
                        h ->
                                MealPlanItemHistoryCO.builder()
                                        .historyId(h.getId())
                                        .oldRecipeName(getRecipeName(h.getOldRecipeId()))
                                        .newRecipeName(getRecipeName(h.getNewRecipeId()))
                                        .adjustReason(
                                                h.getAdjustReason() != null
                                                        ? h.getAdjustReason().name()
                                                        : null)
                                        .adjustedAt(
                                                h.getAdjustedAt() != null
                                                        ? h.getAdjustedAt().format(FORMATTER)
                                                        : null)
                                        .build())
                .collect(Collectors.toList());
    }

    private String getRecipeName(Long recipeId) {
        if (recipeId == null) {
            return null;
        }
        return recipeRepository.findById(recipeId).map(Recipe::getName).orElse(null);
    }
}
