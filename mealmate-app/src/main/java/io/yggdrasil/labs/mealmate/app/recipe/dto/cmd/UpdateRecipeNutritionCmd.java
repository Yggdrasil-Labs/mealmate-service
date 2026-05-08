package io.yggdrasil.labs.mealmate.app.recipe.dto.cmd;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecipeNutritionCmd {

    @NotNull private Long recipeId;

    @Valid private NutritionFactCmd nutritionFact;
}
