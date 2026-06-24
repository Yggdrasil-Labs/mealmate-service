package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.assembler.MealPlanAssembler;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ConfirmPlanCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.ConfirmPlanCO;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.mealplan.exception.MealPlanErrorCode;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.ShoppingItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.PrepPlanRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.PrepPlanDeriveDomainService;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

/** 确认计划命令执行器，派生备菜计划和采购清单。 */
@Component
@RequiredArgsConstructor
public class ConfirmPlanCmdExe {

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final PrepPlanRepository prepPlanRepository;
    private final RecipeRepository recipeRepository;
    private final PrepPlanDeriveDomainService prepPlanDeriveDomainService;

    @Transactional(rollbackFor = Exception.class)
    public ConfirmPlanCO execute(ConfirmPlanCmd cmd) {
        assertPlanOwnership(cmd.getPlanId());
        WeeklyMealPlan plan =
                weeklyMealPlanRepository
                        .findByIdWithItems(cmd.getPlanId())
                        .orElseThrow(() -> new BizException(MealPlanErrorCode.PLAN_NOT_FOUND));

        // 聚合根状态转换
        plan.confirm();

        Map<Long, Recipe> recipeMap = loadRecipeMapForPlan(plan);

        // 派生备菜计划
        PrepPlan prepPlan =
                prepPlanDeriveDomainService.derivePrepPlan(
                        plan.getId(), plan.getItems(), recipeMap);
        PrepPlan savedPrepPlan = prepPlanRepository.save(prepPlan);

        // 派生采购清单
        List<ShoppingItem> shoppingItems =
                prepPlanDeriveDomainService.deriveShoppingList(
                        plan.getId(), plan.getItems(), recipeMap);
        weeklyMealPlanRepository.saveShoppingItems(plan.getId(), shoppingItems);

        // 持久化状态变更
        weeklyMealPlanRepository.updateStatus(plan.getId(), plan.getStatus());

        return MealPlanAssembler.toConfirmPlanCO(
                plan.getId(),
                PlanStatus.CONFIRMED.getCode(),
                savedPrepPlan.getId(),
                savedPrepPlan.getItems() != null ? savedPrepPlan.getItems().size() : 0,
                shoppingItems.size());
    }

    private WeeklyMealPlan assertPlanOwnership(Long planId) {
        return weeklyMealPlanRepository
                .findById(planId)
                .orElseThrow(() -> new BizException(MealPlanErrorCode.PLAN_NOT_FOUND));
    }

    private Map<Long, Recipe> loadRecipeMapForPlan(WeeklyMealPlan plan) {
        if (plan.getItems() == null || plan.getItems().isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> recipeIds =
                plan.getItems().stream()
                        .map(MealPlanItem::getRecipeId)
                        .filter(id -> id != null && id > 0)
                        .distinct()
                        .collect(Collectors.toList());
        if (recipeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 确认计划需要完整 Recipe（含 ingredients）用于派生备菜和采购清单
        return recipeIds.stream()
                .map(recipeRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toMap(Recipe::getId, Function.identity(), (a, b) -> a));
    }
}
