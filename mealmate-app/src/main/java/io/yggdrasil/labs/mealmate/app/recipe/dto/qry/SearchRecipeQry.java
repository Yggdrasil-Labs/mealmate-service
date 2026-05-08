package io.yggdrasil.labs.mealmate.app.recipe.dto.qry;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRecipeQry {

    @NotBlank private String keyword;

    private Integer limit;
}
