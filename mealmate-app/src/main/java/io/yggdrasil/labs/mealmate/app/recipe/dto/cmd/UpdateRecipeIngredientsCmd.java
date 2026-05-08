package io.yggdrasil.labs.mealmate.app.recipe.dto.cmd;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecipeIngredientsCmd {

    @NotNull private Long recipeId;

    @Valid @NotEmpty private List<RecipeIngredientItemCmd> ingredients;
}
