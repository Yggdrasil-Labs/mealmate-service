package io.yggdrasil.labs.mealmate.app.mealplan.application;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import io.yggdrasil.labs.mealmate.app.mealplan.executor.AdjustMealItemCmdExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.GetItemHistoryQryExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.GetRecommendRecipeQryExe;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.ShoppingItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealPlanCrowdType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PrepTaskStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.PrepPlanRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.DuplicateCheckDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.IngredientFilterDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.PrepPlanDeriveDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.WeekPlanGenerateDomainService;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeQueryCriteria;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

/** 周餐计划应用服务，编排领域服务与仓储完成用例。 */
@Service
@Validated
@RequiredArgsConstructor
public class MealPlanAppService {

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final PrepPlanRepository prepPlanRepository;
    private final RecipeRepository recipeRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final MemberPreferenceRepository memberPreferenceRepository;
    private final WeekPlanGenerateDomainService weekPlanGenerateDomainService;
    private final IngredientFilterDomainService ingredientFilterDomainService;
    private final DuplicateCheckDomainService duplicateCheckDomainService;
    private final PrepPlanDeriveDomainService prepPlanDeriveDomainService;
    private final AdjustMealItemCmdExe adjustMealItemCmdExe;
    private final GetRecommendRecipeQryExe getRecommendRecipeQryExe;
    private final GetItemHistoryQryExe getItemHistoryQryExe;

    /** 生成一周计划。 P1-fix: 添加 @Transactional、weekStartDate 周一校验、覆盖旧 DRAFT。 */
    @Transactional(rollbackFor = Exception.class)
    public WeeklyMealPlanCO generateWeeklyPlan(@Valid GenerateWeeklyPlanCmd cmd) {
        // P1: weekStartDate 必须是周一
        if (cmd.getWeekStartDate().getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("PLAN_WEEK_START_DATE_INVALID");
        }

        // TODO: 从认证上下文获取当前家庭 ID，当前为临时方案
        Long familyId = requireFamilyId(cmd.getFamilyId());

        // P2: 覆盖已有 DRAFT——先逻辑删除旧计划 + 物理删除旧 items
        Optional<WeeklyMealPlan> existing =
                weeklyMealPlanRepository.findByFamilyIdAndWeekStartDateForUpdate(
                        familyId, cmd.getWeekStartDate());
        if (existing.isPresent()) {
            WeeklyMealPlan old = existing.get();
            if (old.getStatus() == PlanStatus.CONFIRMED) {
                throw new IllegalStateException("MEAL_PLAN_ALREADY_CONFIRMED");
            }
            weeklyMealPlanRepository.deleteItemsByPlanId(old.getId());
            weeklyMealPlanRepository.logicalDelete(old.getId());
        }

        // 加载候选菜品
        List<Recipe> candidates =
                recipeRepository.page(
                        RecipeQueryCriteria.builder().pageNum(1).pageSize(500).build());

        // 从家庭成员偏好加载忌口/过敏食材
        Set<String> avoidIngredients = new HashSet<>();
        Set<String> allergyIngredients = new HashSet<>();
        familyMemberRepository
                .findByFamilyId(familyId)
                .forEach(
                        member -> {
                            memberPreferenceRepository
                                    .findByMemberId(member.getId())
                                    .ifPresent(
                                            pref -> {
                                                if (pref.getAvoidIngredients() != null) {
                                                    avoidIngredients.addAll(
                                                            pref.getAvoidIngredients());
                                                }
                                                if (pref.getAllergyIngredients() != null) {
                                                    allergyIngredients.addAll(
                                                            pref.getAllergyIngredients());
                                                }
                                            });
                        });

        // 忌口过滤
        candidates =
                ingredientFilterDomainService.filter(
                        candidates, avoidIngredients, allergyIngredients);

        // 生成计划
        WeeklyMealPlan plan =
                weekPlanGenerateDomainService.generate(
                        familyId, cmd.getWeekStartDate(), candidates);

        // 标记重复
        duplicateCheckDomainService.markDuplicates(plan.getItems());

        // 持久化
        WeeklyMealPlan saved = weeklyMealPlanRepository.save(plan);

        // 组装返回
        Map<Long, Recipe> recipeMap = buildRecipeMap(candidates);
        return MealPlanAssembler.toWeeklyMealPlanCO(saved, recipeMap);
    }

    /** 获取当前周计划（支持指定 weekStartDate 查询非本周计划）。 */
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

    /** 获取计划详情。 */
    public WeeklyMealPlanCO getPlanDetail(GetMealPlanDetailQry qry) {
        assertPlanOwnership(qry.getPlanId());
        WeeklyMealPlan plan =
                weeklyMealPlanRepository
                        .findByIdWithItems(qry.getPlanId())
                        .orElseThrow(() -> new IllegalArgumentException("PLAN_NOT_FOUND"));
        Map<Long, Recipe> recipeMap = loadRecipeMapForPlan(plan);
        return MealPlanAssembler.toWeeklyMealPlanCO(plan, recipeMap);
    }

    /** 替换计划条目的菜品。P1-fix: 添加状态守卫。 */
    @Transactional(rollbackFor = Exception.class)
    public void replaceItem(@Valid ReplaceItemCmd cmd) {
        assertPlanDraft(cmd.getPlanId());
        MealPlanItem item =
                weeklyMealPlanRepository
                        .findItemById(cmd.getItemId())
                        .orElseThrow(() -> new IllegalArgumentException("ITEM_NOT_FOUND"));
        if (!cmd.getPlanId().equals(item.getPlanId())) {
            throw new IllegalArgumentException("ITEM_NOT_FOUND");
        }
        item.setRecipeId(cmd.getRecipeId());
        weeklyMealPlanRepository.saveItem(item);
    }

    /** 添加计划条目。P1-fix: 添加状态守卫。 */
    @Transactional(rollbackFor = Exception.class)
    public void addItem(@Valid AddItemCmd cmd) {
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

    /** 删除计划条目。 P1-fix: 添加状态守卫 + 最后一项不可删除校验。 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(DeleteItemCmd cmd) {
        MealPlanItem item =
                weeklyMealPlanRepository
                        .findItemById(cmd.getItemId())
                        .orElseThrow(() -> new IllegalArgumentException("ITEM_NOT_FOUND"));
        if (!cmd.getPlanId().equals(item.getPlanId())) {
            throw new IllegalArgumentException("ITEM_NOT_FOUND");
        }
        assertPlanDraft(item.getPlanId());

        // P1: 最后一项不可删除——检查同 planId+mealDate+mealType 的 item 数量
        WeeklyMealPlan plan =
                weeklyMealPlanRepository
                        .findByIdWithItems(item.getPlanId())
                        .orElseThrow(() -> new IllegalArgumentException("PLAN_NOT_FOUND"));
        long sameSlotCount =
                plan.getItems().stream()
                        .filter(
                                i ->
                                        i.getMealDate().equals(item.getMealDate())
                                                && i.getMealType() == item.getMealType())
                        .count();
        if (sameSlotCount <= 1) {
            throw new IllegalStateException("MEAL_PLAN_ITEM_LAST_ONE");
        }

        weeklyMealPlanRepository.deleteItem(cmd.getItemId());
    }

    /** 手动添加条目（按菜名查找或仅记录名称）。P1-fix: 添加状态守卫。 */
    @Transactional(rollbackFor = Exception.class)
    public void manualAddItem(@Valid ManualAddItemCmd cmd) {
        assertPlanDraft(cmd.getPlanId());
        // P2-fix: recipe 未找到时使用占位 ID 0 而非 null（DDL NOT NULL 约束）
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

    /** 确认计划，派生备菜计划和采购清单。 P1-fix: 添加 @Transactional + 状态重复确认校验。 */
    @Transactional(rollbackFor = Exception.class)
    public ConfirmPlanCO confirmPlan(ConfirmPlanCmd cmd) {
        assertPlanOwnership(cmd.getPlanId());
        WeeklyMealPlan plan =
                weeklyMealPlanRepository
                        .findByIdWithItems(cmd.getPlanId())
                        .orElseThrow(() -> new IllegalArgumentException("PLAN_NOT_FOUND"));

        // P1: 已确认不可重复确认
        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw new IllegalStateException("MEAL_PLAN_ALREADY_CONFIRMED");
        }

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

        // 更新状态
        weeklyMealPlanRepository.updateStatus(plan.getId(), PlanStatus.CONFIRMED);

        return MealPlanAssembler.toConfirmPlanCO(
                plan.getId(),
                PlanStatus.CONFIRMED.getCode(),
                savedPrepPlan.getId(),
                savedPrepPlan.getItems() != null ? savedPrepPlan.getItems().size() : 0,
                shoppingItems.size());
    }

    /** 获取备菜计划。 */
    public PrepPlanCO getPrepPlan(GetPrepPlanQry qry) {
        assertPlanOwnership(qry.getPlanId());
        PrepPlan plan = prepPlanRepository.findByPlanId(qry.getPlanId()).orElse(null);
        return MealPlanAssembler.toPrepPlanCO(plan);
    }

    /** 更新备菜条目状态。 */
    @Transactional(rollbackFor = Exception.class)
    public void updatePrepItemStatus(@Valid UpdatePrepItemStatusCmd cmd) {
        assertPlanOwnership(cmd.getPlanId());
        prepPlanRepository.updateItemStatus(
                cmd.getItemId(), PrepTaskStatus.valueOf(cmd.getStatus()));
    }

    /** 获取采购清单。 */
    public List<ShoppingItemCO> getShoppingList(GetShoppingListQry qry) {
        assertPlanOwnership(qry.getPlanId());
        List<ShoppingItem> items =
                weeklyMealPlanRepository.findShoppingItemsByPlanId(qry.getPlanId());
        return MealPlanAssembler.toShoppingItemCOs(items);
    }

    /** 更新采购条目状态。 */
    @Transactional(rollbackFor = Exception.class)
    public void updateShoppingItem(@Valid UpdateShoppingItemCmd cmd) {
        assertPlanOwnership(cmd.getPlanId());
        weeklyMealPlanRepository.updateShoppingItemPurchased(cmd.getItemId(), cmd.getPurchased());
    }

    /** 替换餐次菜品（带调整历史记录）。 */
    @Transactional(rollbackFor = Exception.class)
    public MealPlanItemCO adjustMealItem(@Valid AdjustMealItemCmd cmd) {
        return adjustMealItemCmdExe.execute(cmd);
    }

    /** 获取推荐菜品列表。 */
    public List<RecipeBriefCO> getRecommendRecipes(GetRecommendRecipeQry qry) {
        return getRecommendRecipeQryExe.execute(qry);
    }

    /** 获取条目调整历史。 */
    public List<MealPlanItemHistoryCO> getItemHistory(GetItemHistoryQry qry) {
        return getItemHistoryQryExe.execute(qry);
    }

    // ─── 内部辅助 ───

    /** P1: 检查计划状态必须为 DRAFT 才允许编辑操作。 非 DRAFT 抛出 MEAL_PLAN_ALREADY_CONFIRMED 异常。 */
    private void assertPlanDraft(Long planId) {
        WeeklyMealPlan plan = assertPlanOwnership(planId);
        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw new IllegalStateException("MEAL_PLAN_ALREADY_CONFIRMED");
        }
    }

    /** 验证 planId 存在性。 TODO: 接入认证后恢复 familyId 归属校验，防止 IDOR 越权。 */
    private WeeklyMealPlan assertPlanOwnership(Long planId) {
        return weeklyMealPlanRepository
                .findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("PLAN_NOT_FOUND"));
    }

    /** 获取当前家庭 ID。优先使用显式传入值，否则从认证上下文获取。 TODO: 接入 Spring Security 后从 SecurityContextHolder 获取。 */
    private Long requireFamilyId(Long familyId) {
        if (familyId != null) {
            return familyId;
        }
        throw new IllegalStateException("FAMILY_ID_REQUIRED");
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
        // P2: 逐个查询（N+1），待 RecipeRepository 添加 findByIds 批量方法后优化
        return recipeIds.stream()
                .map(recipeRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Recipe::getId, Function.identity()));
    }

    private Map<Long, Recipe> buildRecipeMap(List<Recipe> recipes) {
        return recipes.stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity(), (a, b) -> a));
    }
}
