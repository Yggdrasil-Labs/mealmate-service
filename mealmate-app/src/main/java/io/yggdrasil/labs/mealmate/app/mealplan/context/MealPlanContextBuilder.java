package io.yggdrasil.labs.mealmate.app.mealplan.context;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberTargetType;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.IngredientFilterDomainService;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeQueryCriteria;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

/**
 * 周餐计划 AI 上下文构建器。
 *
 * <p>汇聚家庭画像、偏好、候选菜品等信息，组装为 {@link MealPlanContext} 供 prompt 和 fallback 使用。
 *
 * <p>设计要点：
 *
 * <ul>
 *   <li>familySummary 使用角色代称（成人/宝宝/访客），不暴露真实姓名
 *   <li>偏好汇总聚合所有成员的忌口、过敏和口味标签
 *   <li>候选菜品经过忌口/过敏过滤，最多保留 {@value #MAX_RECIPES} 道
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class MealPlanContextBuilder {

    /** 候选菜品上限，避免 prompt token 超限。 */
    static final int MAX_RECIPES = 80;

    private final FamilyMemberRepository familyMemberRepository;
    private final MemberPreferenceRepository memberPreferenceRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientFilterDomainService ingredientFilterDomainService;

    /**
     * 构建 AI 配餐上下文。
     *
     * @param familyId 目标家庭 ID
     * @return 汇聚后的配餐上下文
     */
    public MealPlanContext build(Long familyId) {
        // 1. 加载家庭成员
        List<FamilyMember> members = familyMemberRepository.findByFamilyId(familyId);

        // 2. 构建 familySummary（用角色代称，不用真实姓名，保护隐私）
        String familySummary = buildFamilySummary(members);

        // 3. 加载所有成员偏好，汇总忌口/过敏/口味
        Set<String> avoidIngredients = new HashSet<>();
        Set<String> allergyIngredients = new HashSet<>();
        List<String> tasteNotes = new ArrayList<>();
        for (FamilyMember m : members) {
            memberPreferenceRepository
                    .findByMemberId(m.getId())
                    .ifPresent(
                            pref -> {
                                aggregatePreference(
                                        pref, avoidIngredients, allergyIngredients, tasteNotes);
                            });
        }
        String preferenceSummary =
                buildPreferenceSummary(avoidIngredients, allergyIngredients, tasteNotes);

        // 4. 加载菜品库 + 忌口/过敏过滤
        List<Recipe> allRecipes =
                recipeRepository.page(
                        RecipeQueryCriteria.builder().pageNum(1).pageSize(500).build());
        List<Recipe> filtered =
                ingredientFilterDomainService.filter(
                        allRecipes, avoidIngredients, allergyIngredients);

        // 5. 截取前 MAX_RECIPES 道，生成摘要
        List<Recipe> candidates =
                filtered.size() > MAX_RECIPES ? filtered.subList(0, MAX_RECIPES) : filtered;
        List<Long> candidateIds =
                candidates.stream().map(Recipe::getId).collect(Collectors.toList());
        String recipeCatalog = buildRecipeCatalog(candidates);

        return MealPlanContext.builder()
                .familySummary(familySummary)
                .preferenceSummary(preferenceSummary)
                .recipeCatalog(recipeCatalog)
                .candidateIds(candidateIds)
                .candidateRecipes(candidates)
                .avoidIngredients(avoidIngredients)
                .allergyIngredients(allergyIngredients)
                .build();
    }

    // ─── 私有方法 ───

    private String buildFamilySummary(List<FamilyMember> members) {
        StringBuilder sb = new StringBuilder();
        int idx = 1;
        for (FamilyMember m : members) {
            String role = mapRole(m.getRoleType());
            String ageRange = calcAgeRange(m.getBirthday());
            String target = mapTarget(m.getTargetType());
            sb.append(String.format("- 成员%d：%s，%s，目标：%s\n", idx++, role, ageRange, target));
        }
        return sb.toString();
    }

    /** 将偏好中的忌口、过敏和口味标签聚合到外部集合。 */
    private void aggregatePreference(
            MemberPreference pref,
            Set<String> avoidIngredients,
            Set<String> allergyIngredients,
            List<String> tasteNotes) {
        if (pref.getAvoidIngredients() != null) {
            avoidIngredients.addAll(pref.getAvoidIngredients());
        }
        if (pref.getAllergyIngredients() != null) {
            allergyIngredients.addAll(pref.getAllergyIngredients());
        }
        if (pref.getTasteTags() != null) {
            tasteNotes.addAll(pref.getTasteTags());
        }
    }

    /**
     * 映射角色枚举到自然语言代称。
     *
     * <p>当前 {@link MemberRoleType} 枚举值: ADULT, BABY, GUEST。
     */
    String mapRole(MemberRoleType roleType) {
        if (roleType == null) {
            return "家人";
        }
        return switch (roleType) {
            case ADULT -> "成人";
            case BABY -> "宝宝";
            case GUEST -> "访客";
        };
    }

    /** 根据生日计算年龄段描述。 */
    String calcAgeRange(LocalDate birthday) {
        if (birthday == null) {
            return "年龄未知";
        }
        int age = Period.between(birthday, LocalDate.now()).getYears();
        if (age <= 1) return "0-1岁";
        if (age <= 3) return "1-3岁";
        if (age <= 6) return "3-6岁";
        if (age <= 12) return "6-12岁";
        if (age <= 18) return "12-18岁";
        if (age <= 30) return "18-30岁";
        if (age <= 40) return "30-40岁";
        if (age <= 50) return "40-50岁";
        if (age <= 60) return "50-60岁";
        return "60岁以上";
    }

    /**
     * 映射饮食目标枚举到自然语言描述。
     *
     * <p>当前 {@link MemberTargetType} 枚举值: BALANCED, WEIGHT_LOSS, MUSCLE_GAIN, HEALTH_MANAGEMENT。
     */
    String mapTarget(MemberTargetType targetType) {
        if (targetType == null) {
            return "均衡饮食";
        }
        return switch (targetType) {
            case BALANCED -> "均衡饮食";
            case WEIGHT_LOSS -> "减脂";
            case MUSCLE_GAIN -> "增肌";
            case HEALTH_MANAGEMENT -> "健康管理";
        };
    }

    /** 将忌口、过敏、口味汇总为自然语言描述。 */
    private String buildPreferenceSummary(
            Set<String> avoid, Set<String> allergy, List<String> tastes) {
        StringBuilder sb = new StringBuilder();
        if (!avoid.isEmpty()) {
            sb.append("忌口：").append(String.join("、", avoid)).append("\n");
        }
        if (!allergy.isEmpty()) {
            sb.append("过敏：").append(String.join("、", allergy)).append("\n");
        }
        if (!tastes.isEmpty()) {
            sb.append("口味偏好：").append(String.join("、", tastes)).append("\n");
        }
        return sb.length() > 0 ? sb.toString().trim() : "无特殊约束";
    }

    /** 生成候选菜品的文本目录，供 AI prompt 引用。 */
    private String buildRecipeCatalog(List<Recipe> recipes) {
        StringBuilder sb = new StringBuilder();
        for (Recipe r : recipes) {
            sb.append(String.format("ID:%d %s", r.getId(), r.getName()));
            if (r.getRecipeType() != null) {
                sb.append(" [").append(r.getRecipeType()).append("]");
            }
            if (Boolean.TRUE.equals(r.getBabyFriendly())) {
                sb.append(" 宝宝友好");
            }
            if (Boolean.TRUE.equals(r.getWeightLossFriendly())) {
                sb.append(" 减脂友好");
            }
            if (r.getCookingTimeMin() != null) {
                sb.append(" ").append(r.getCookingTimeMin()).append("min");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }
}
