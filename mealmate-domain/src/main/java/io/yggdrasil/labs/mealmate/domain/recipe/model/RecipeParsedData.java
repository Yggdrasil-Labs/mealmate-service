package io.yggdrasil.labs.mealmate.domain.recipe.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 菜品解析中间态值对象。
 *
 * <p>表达 LLM 逐步解析菜品的中间结果，所有字段允许 null（渐进填充）。 枚举字段用 String 承接 LLM 原始输出，confirm 时再转换为具体枚举类型。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeParsedData {

    private String name;

    /** 菜品类型，对应 RecipeType 枚举字符串 */
    private String recipeType;

    /** 季节标签，对应 SeasonTag 枚举字符串 */
    private String seasonTag;

    /** 适用人群标签，对应 CrowdTag 枚举字符串 */
    private String crowdTag;

    /** 口味标签列表 */
    private List<String> tasteTags;

    /** 难度等级，对应 DifficultyLevel 枚举字符串 */
    private String difficultyLevel;

    /** 烹饪时间（分钟） */
    private Integer cookingTimeMin;

    /** 是否宝宝友好 */
    private Boolean babyFriendly;

    /** 是否适合减脂 */
    private Boolean weightLossFriendly;

    /** 食材列表 */
    private List<IngredientItem> ingredients;

    /** 烹饪步骤列表。null 表示未填写；空列表表示明确无步骤 */
    private List<StepItem> steps;

    /** 营养信息 */
    private NutritionItem nutritionFact;

    /** 食材项。 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientItem {
        private String ingredientName;

        /** 食材类型，对应 IngredientType 枚举字符串 */
        private String ingredientType;

        private Double quantity;
        private String unit;
        private Boolean mainIngredient;
    }

    /** 烹饪步骤项。 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepItem {
        private Integer stepNo;
        private String content;
    }

    /** 营养信息项。 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionItem {
        /** 热量（kcal） */
        private Double calories;

        /** 蛋白质（g） */
        private Double protein;

        /** 脂肪（g） */
        private Double fat;

        /** 碳水化合物（g） */
        private Double carbohydrate;
    }
}
