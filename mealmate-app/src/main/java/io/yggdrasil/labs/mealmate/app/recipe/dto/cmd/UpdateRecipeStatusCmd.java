package io.yggdrasil.labs.mealmate.app.recipe.dto.cmd;

import jakarta.validation.constraints.NotNull;

import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecipeStatusCmd {

    @NotNull private Long recipeId;

    @NotNull private RecipeStatus status;
}
