package io.yggdrasil.labs.mealmate.app.recipe.dto.co;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStepCO {

    private Long id;
    private Long recipeId;
    private Integer stepNo;
    private String content;
    private String imageUrl;
}
