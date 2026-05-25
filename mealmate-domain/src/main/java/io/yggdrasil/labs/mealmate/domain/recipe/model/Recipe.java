package io.yggdrasil.labs.mealmate.domain.recipe.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.SeasonTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 菜谱聚合根。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    private Long id;
    private String name;
    private RecipeType recipeType;
    private RecipeSourceType sourceType;
    private SeasonTag seasonTag;
    private CrowdTag crowdTag;
    private List<String> tasteTags;
    private DifficultyLevel difficultyLevel;
    private Integer cookingTimeMin;
    private String coverImageUrl;
    private Boolean babyFriendly;
    private Boolean weightLossFriendly;
    private RecipeStatus status;
    private List<RecipeIngredient> ingredients;
    private List<RecipeStep> steps;
    private NutritionFact nutritionFact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long deleted;

    // ─── 领域行为 ───

    /** 规范化聚合根所有字段并校验不变式。 */
    public void normalize() {
        this.tasteTags = normalizeTasteTags(this.tasteTags);
        this.ingredients = normalizeIngredients(this.ingredients);
        this.steps = normalizeSteps(this.steps);
        applyBabyFriendlyRule();
        if (this.nutritionFact != null) {
            validateNutritionFact(this.nutritionFact);
        }
    }

    /** 校验菜谱可编辑（系统来源菜谱不可编辑）。 */
    public void assertEditable() {
        if (this.sourceType == null || this.sourceType == RecipeSourceType.SYSTEM) {
            throw new IllegalArgumentException("Recipe cannot be edited");
        }
    }

    /** 校验菜谱可删除（系统来源菜谱不可删除）。 */
    public void assertDeletable() {
        if (this.sourceType == null || this.sourceType == RecipeSourceType.SYSTEM) {
            throw new IllegalArgumentException("Recipe cannot be deleted");
        }
    }

    /** 规范化口味标签列表。 */
    public static List<String> normalizeTasteTags(List<String> tasteTags) {
        if (tasteTags == null) {
            return Collections.emptyList();
        }
        return tasteTags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /** 规范化食材列表（不能为空，自动编号）。 */
    public static List<RecipeIngredient> normalizeIngredients(List<RecipeIngredient> ingredients) {
        List<RecipeIngredient> result = normalizeChildList(ingredients);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Recipe ingredients cannot be empty");
        }
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setSortNo(i + 1);
        }
        return result;
    }

    /** 规范化步骤列表（可为空，自动编号）。 */
    public static List<RecipeStep> normalizeSteps(List<RecipeStep> steps) {
        List<RecipeStep> result = normalizeChildList(steps);
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setStepNo(i + 1);
        }
        return result;
    }

    /** 校验营养值非负。 */
    public static void validateNutritionFact(NutritionFact nf) {
        if (nf == null) {
            return;
        }
        if (isNegative(nf.getCalories())
                || isNegative(nf.getProtein())
                || isNegative(nf.getFat())
                || isNegative(nf.getCarbohydrate())
                || isNegative(nf.getFiber())
                || isNegative(nf.getCalcium())
                || isNegative(nf.getSodium())) {
            throw new IllegalArgumentException("Nutrition values cannot be negative");
        }
    }

    private void applyBabyFriendlyRule() {
        if (this.crowdTag == CrowdTag.BABY) {
            this.babyFriendly = Boolean.TRUE;
        }
    }

    private static <T> List<T> normalizeChildList(List<T> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        return values.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }
}
