package io.yggdrasil.labs.mealmate.app.recipe.dto.cmd;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStepItemCmd {

    private Integer stepNo;

    @NotBlank private String content;

    private String imageUrl;
}
