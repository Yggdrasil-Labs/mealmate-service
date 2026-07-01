package io.yggdrasil.labs.mealmate.domain.recipe.model;

import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeParseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 菜品解析缓存值对象。
 *
 * <p>承载多轮对话的累积解析结果、当前状态和确认后的菜品 ID。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeParseCache {

    /** 累积的解析结果 */
    private RecipeParsedData accumulatedParsed;

    /** 当前解析状态 */
    private RecipeParseStatus status;

    /** 确认后关联的菜品 ID，仅 CONFIRMED 状态时非空 */
    private Long confirmedRecipeId;
}
