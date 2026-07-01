package io.yggdrasil.labs.mealmate.app.recipe.dto.cmd;

import jakarta.validation.constraints.NotBlank;

import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParsedData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** AI 菜品确认入库命令。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRecipeConfirmCmd {

    @NotBlank private String sessionId;

    /** 用户可编辑后的解析数据 */
    private RecipeParsedData recipe;
}
