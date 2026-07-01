package io.yggdrasil.labs.mealmate.app.recipe.dto.co;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** AI 菜品确认入库结果 CO。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRecipeConfirmResultCO {

    private Long recipeId;
}
