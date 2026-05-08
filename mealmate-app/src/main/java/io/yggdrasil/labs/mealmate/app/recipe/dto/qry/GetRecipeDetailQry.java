package io.yggdrasil.labs.mealmate.app.recipe.dto.qry;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetRecipeDetailQry {

    @NotNull private Long recipeId;
}
