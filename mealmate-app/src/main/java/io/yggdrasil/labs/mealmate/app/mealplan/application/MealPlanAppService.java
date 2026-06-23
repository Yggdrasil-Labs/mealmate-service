package io.yggdrasil.labs.mealmate.app.mealplan.application;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.assembler.MealPlanAssembler;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AddItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AdjustMealItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ConfirmPlanCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.DeleteItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.GenerateWeeklyPlanCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ManualAddItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ReplaceItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.UpdatePrepItemStatusCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.UpdateShoppingItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.ConfirmPlanCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.MealPlanItemCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.MealPlanItemHistoryCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.PrepPlanCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.RecipeBriefCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.ShoppingItemCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.WeeklyMealPlanCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetCurrentWeekPlanQry;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetItemHistoryQry;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetMealPlanDetailQry;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetPrepPlanQry;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetRecommendRecipeQry;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetShoppingListQry;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.AddItemCmdExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.AdjustMealItemCmdExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.ConfirmPlanCmdExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.DeleteItemCmdExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.GenerateWeeklyPlanCmdExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.GetItemHistoryQryExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.GetRecommendRecipeQryExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.ManualAddItemCmdExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.ReplaceItemCmdExe;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.mealplan.exception.MealPlanErrorCode;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.ShoppingItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PrepTaskStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.PrepPlanRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

/** 周餐计划应用服务，纯委派编排。 */
@Service
@Validated
@RequiredArgsConstructor
public class MealPlanAppService {

    private final GenerateWeeklyPlanCmdExe generateWeeklyPlanCmdExe;
    private final ConfirmPlanCmdExe confirmPlanCmdExe;
    private final DeleteItemCmdExe deleteItemCmdExe;
    private final ReplaceItemCmdExe replaceItemCmdExe;
    private final AddItemCmdExe addItemCmdExe;
    private final ManualAddItemCmdExe manualAddItemCmdExe;
    private final AdjustMealItemCmdExe adjustMealItemCmdExe;
    private final GetRecommendRecipeQryExe getRecommendRecipeQryExe;
    private final GetItemHistoryQryExe getItemHistoryQryExe;
    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final PrepPlanRepository prepPlanRepository;
    private final RecipeRepository recipeRepository;

    // ─── Commands ───

    public WeeklyMealPlanCO generateWeeklyPlan(@Valid GenerateWeeklyPlanCmd cmd) {
        return generateWeeklyPlanCmdExe.execute(cmd);
    }

    public ConfirmPlanCO confirmPlan(ConfirmPlanCmd cmd) {
        return confirmPlanCmdExe.execute(cmd);
    }

    public void deleteItem(DeleteItemCmd cmd) {
        deleteItemCmdExe.execute(cmd);
    }

    public void replaceItem(@Valid ReplaceItemCmd cmd) {
        replaceItemCmdExe.execute(cmd);
    }

    public void addItem(@Valid AddItemCmd cmd) {
        addItemCmdExe.execute(cmd);
    }

    public void manualAddItem(@Valid ManualAddItemCmd cmd) {
        manualAddItemCmdExe.execute(cmd);
    }

    public MealPlanItemCO adjustMealItem(@Valid AdjustMealItemCmd cmd) {
        return adjustMealItemCmdExe.execute(cmd);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePrepItemStatus(@Valid UpdatePrepItemStatusCmd cmd) {
        assertPlanOwnership(cmd.getPlanId());
        prepPlanRepository.updateItemStatus(
                cmd.getItemId(), PrepTaskStatus.valueOf(cmd.getStatus()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateShoppingItem(@Valid UpdateShoppingItemCmd cmd) {
        assertPlanOwnership(cmd.getPlanId());
        weeklyMealPlanRepository.updateShoppingItemPurchased(cmd.getItemId(), cmd.getPurchased());
    }

    // ─── Queries ───

    public WeeklyMealPlanCO getCurrentWeekPlan(GetCurrentWeekPlanQry qry) {
        Long familyId = requireFamilyId(qry.getFamilyId());
        LocalDate monday =
                qry.getWeekStartDate() != null
                        ? qry.getWeekStartDate()
                        : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Optional<WeeklyMealPlan> opt =
                weeklyMealPlanRepository.findByFamilyIdAndWeekStartDate(familyId, monday);
        if (opt.isEmpty()) {
            return null;
        }
        WeeklyMealPlan plan = opt.get();
        Map<Long, Recipe> recipeMap = loadRecipeMapForPlan(plan);
        return MealPlanAssembler.toWeeklyMealPlanCO(plan, recipeMap);
    }

    public WeeklyMealPlanCO getPlanDetail(GetMealPlanDetailQry qry) {
        assertPlanOwnership(qry.getPlanId());
        WeeklyMealPlan plan =
                weeklyMealPlanRepository
                        .findByIdWithItems(qry.getPlanId())
                        .orElseThrow(() -> new BizException(MealPlanErrorCode.PLAN_NOT_FOUND));
        Map<Long, Recipe> recipeMap = loadRecipeMapForPlan(plan);
        return MealPlanAssembler.toWeeklyMealPlanCO(plan, recipeMap);
    }

    public PrepPlanCO getPrepPlan(GetPrepPlanQry qry) {
        assertPlanOwnership(qry.getPlanId());
        PrepPlan plan = prepPlanRepository.findByPlanId(qry.getPlanId()).orElse(null);
        return MealPlanAssembler.toPrepPlanCO(plan);
    }

    public List<ShoppingItemCO> getShoppingList(GetShoppingListQry qry) {
        assertPlanOwnership(qry.getPlanId());
        List<ShoppingItem> items =
                weeklyMealPlanRepository.findShoppingItemsByPlanId(qry.getPlanId());
        return MealPlanAssembler.toShoppingItemCOs(items);
    }

    public List<RecipeBriefCO> getRecommendRecipes(GetRecommendRecipeQry qry) {
        return getRecommendRecipeQryExe.execute(qry);
    }

    public List<MealPlanItemHistoryCO> getItemHistory(GetItemHistoryQry qry) {
        return getItemHistoryQryExe.execute(qry);
    }

    // ─── 内部辅助 ───

    private WeeklyMealPlan assertPlanOwnership(Long planId) {
        return weeklyMealPlanRepository
                .findById(planId)
                .orElseThrow(() -> new BizException(MealPlanErrorCode.PLAN_NOT_FOUND));
    }

    private Long requireFamilyId(Long familyId) {
        if (familyId != null) {
            return familyId;
        }
        throw new BizException(MealPlanErrorCode.FAMILY_ID_REQUIRED);
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
        return recipeRepository.findByIds(recipeIds).stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity(), (a, b) -> a));
    }
}
