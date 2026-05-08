package io.yggdrasil.labs.mealmate.domain.recipe.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 菜谱步骤子对象。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStep {

    private Long id;
    private Long recipeId;
    private Integer stepNo;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
