package io.yggdrasil.labs.mealmate.app.mealplan.dto.co;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeBriefCO {

    private Long recipeId;
    private String name;
    private String recipeType;
    private String seasonTag;
    private String coverImageUrl;
    private Integer cookTimeMinutes;
}
