package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.RecipeBriefCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetRecommendRecipeQry;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.MealPlanRuleDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.RecipeRecommendDomainService;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeQueryCriteria;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

/** 推荐菜品查询执行器：排除周内已用菜品后返回候选。 */
@Component
@RequiredArgsConstructor
public class GetRecommendRecipeQryExe {

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final RecipeRepository recipeRepository;
    private final MealPlanRuleDomainService ruleDomainService;
    private final RecipeRecommendDomainService recommendDomainService;

    public List<RecipeBriefCO> execute(GetRecommendRecipeQry qry) {
        // 1. 加载计划
        WeeklyMealPlan plan =
                weeklyMealPlanRepository
                        .findByIdWithItems(qry.getPlanId())
                        .orElseThrow(() -> new IllegalArgumentException("MEAL_PLAN_NOT_FOUND"));

        // 2. 校验条目存在
        plan.getItems().stream()
                .filter(i -> i.getId().equals(qry.getItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("MEAL_PLAN_ITEM_NOT_FOUND"));

        // 3. 获取已用菜品 ID
        Set<Long> usedIds = ruleDomainService.getUsedRecipeIds(plan.getItems());

        // 4. 加载候选菜品
        List<Recipe> candidates =
                recipeRepository.page(
                        RecipeQueryCriteria.builder().pageNum(1).pageSize(500).build());

        // 5. 推荐排除已用
        List<Recipe> result = recommendDomainService.recommend(candidates, usedIds, 20);

        // 6. 转为 CO
        return result.stream()
                .map(
                        r ->
                                RecipeBriefCO.builder()
                                        .recipeId(r.getId())
                                        .name(r.getName())
                                        .recipeType(
                                                r.getRecipeType() != null
                                                        ? r.getRecipeType().name()
                                                        : null)
                                        .seasonTag(
                                                r.getSeasonTag() != null
                                                        ? r.getSeasonTag().name()
                                                        : null)
                                        .coverImageUrl(r.getCoverImageUrl())
                                        .cookTimeMinutes(r.getCookingTimeMin())
                                        .build())
                .collect(Collectors.toList());
    }
}
