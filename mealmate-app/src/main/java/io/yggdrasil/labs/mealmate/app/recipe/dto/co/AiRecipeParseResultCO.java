package io.yggdrasil.labs.mealmate.app.recipe.dto.co;

import java.util.List;

import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParsedData;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeParseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** AI 菜品解析结果 CO。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecipeParseResultCO {

    private String sessionId;
    private String reply;
    private RecipeParsedData parsed;
    private RecipeParseStatus status;
    private List<String> suggestions;
}
