package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.assembler.MealPlanAssembler;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.GenerateWeeklyPlanCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.WeeklyMealPlanCO;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.exception.MealPlanErrorCode;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.DuplicateCheckDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.IngredientFilterDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.WeekPlanGenerateDomainService;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeQueryCriteria;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

/** 生成一周计划命令执行器。 */
@Component
@RequiredArgsConstructor
public class GenerateWeeklyPlanCmdExe {

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final RecipeRepository recipeRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final MemberPreferenceRepository memberPreferenceRepository;
    private final WeekPlanGenerateDomainService weekPlanGenerateDomainService;
    private final IngredientFilterDomainService ingredientFilterDomainService;
    private final DuplicateCheckDomainService duplicateCheckDomainService;

    @Transactional(rollbackFor = Exception.class)
    public WeeklyMealPlanCO execute(@Valid GenerateWeeklyPlanCmd cmd) {
        if (cmd.getWeekStartDate().getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new BizException(MealPlanErrorCode.PLAN_WEEK_START_DATE_INVALID);
        }

        Long familyId = requireFamilyId(cmd.getFamilyId());

        // 覆盖已有 DRAFT——先逻辑删除旧计划 + 物理删除旧 items
        Optional<WeeklyMealPlan> existing =
                weeklyMealPlanRepository.findByFamilyIdAndWeekStartDateForUpdate(
                        familyId, cmd.getWeekStartDate());
        if (existing.isPresent()) {
            WeeklyMealPlan old = existing.get();
            if (old.getStatus() == PlanStatus.CONFIRMED) {
                throw new BizException(MealPlanErrorCode.PLAN_ALREADY_CONFIRMED);
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

    private Long requireFamilyId(Long familyId) {
        if (familyId != null) {
            return familyId;
        }
        throw new BizException(MealPlanErrorCode.FAMILY_ID_REQUIRED);
    }

    private Map<Long, Recipe> buildRecipeMap(List<Recipe> recipes) {
        return recipes.stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity(), (a, b) -> a));
    }
}
